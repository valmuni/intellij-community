// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.openapi.projectRoots.impl;

import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.project.RootsChangeRescanningInfo;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.RootProvider;
import com.intellij.openapi.roots.ex.ProjectRootManagerEx;
import com.intellij.openapi.roots.impl.RootProviderBaseImpl;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import com.intellij.util.indexing.BuildableRootsChangeRescanningInfo;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.util.function.Supplier;

/**
 * @deprecated The key difference between `MockSdk` and real SDK in root provider and absent of `SdkModificator`, so
 * it doesn't make sense to use in at all. Please use regular API {@link ProjectJdkTable}. All usages from the
 * platform were removed, so the class will be removed in a couple of months.
 */
@TestOnly
@Deprecated
@ApiStatus.ScheduledForRemoval(inVersion = "2024.2")
public class MockSdk implements Sdk, SdkModificator {
  private String myName;
  private String myHomePath;
  @NotNull private String myVersionString;
  private final MultiMap<OrderRootType, VirtualFile> myRoots;
  private final @NotNull Supplier<? extends SdkTypeId> mySdkType;
  private SdkAdditionalData myData;
  private final MyRootProvider myRootProvider = new MyRootProvider();

  public MockSdk(@NotNull String name,
                 @NotNull String homePath,
                 @NotNull String versionString,
                 @NotNull MultiMap<OrderRootType, VirtualFile> roots,
                 @NotNull SdkTypeId sdkType) {
    myName = name;
    myHomePath = homePath;
    myVersionString = versionString;
    myRoots = roots;
    mySdkType = () -> sdkType;
  }

  @NotNull
  @Override
  public SdkTypeId getSdkType() {
    return mySdkType.get();
  }

  @NotNull
  @Override
  public String getName() {
    return myName;
  }

  @NotNull
  @Override
  public String getVersionString() {
    return myVersionString;
  }

  @Override
  public String getHomePath() {
    return myHomePath;
  }

  @Nullable
  @Override
  public VirtualFile getHomeDirectory() {
    return StandardFileSystems.local().findFileByPath(myHomePath);
  }

  @Nullable
  @Override
  public SdkAdditionalData getSdkAdditionalData() {
    return myData;
  }

  @NotNull
  @Override
  public Sdk clone() {
    return new MockSdk(myName, myHomePath, myVersionString, new MultiMap<>(myRoots), mySdkType.get()) {
      private final UserDataHolder udh = new UserDataHolderBase();

      @NotNull
      @Override
      public SdkModificator getSdkModificator() {
        return this;
      }

      @Nullable
      @Override
      public <T> T getUserData(@NotNull Key<T> key) {
        return udh.getUserData(key);
      }

      @Override
      public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
        udh.putUserData(key, value);
      }
    };
  }

  @NotNull
  @Override
  public SdkModificator getSdkModificator() {
    throwReadOnly();
    return null;
  }

  @Override
  public VirtualFile @NotNull [] getRoots(@NotNull OrderRootType rootType) {
    return myRoots.get(rootType).toArray(VirtualFile.EMPTY_ARRAY);
  }

  @Override
  public void setName(@NotNull String name) {
    myName = name;
  }

  @Override
  public void setHomePath(String path) {
    myHomePath = path;
  }

  @Override
  public void setVersionString(@NotNull String versionString) {
    myVersionString = versionString;
  }

  @Override
  public void setSdkAdditionalData(SdkAdditionalData data) {
    myData = data;
  }

  @Override
  public void addRoot(@NotNull VirtualFile root, @NotNull OrderRootType rootType) {
    myRoots.putValue(rootType, root);
  }

  @Override
  public void removeRoot(@NotNull VirtualFile root, @NotNull OrderRootType rootType) {
    myRoots.remove(rootType, root);
  }

  @Override
  public void removeRoots(@NotNull OrderRootType rootType) {
    myRoots.remove(rootType);
  }

  @Override
  public void removeAllRoots() {
    myRoots.clear();
  }

  @Override
  public void commitChanges() {
    WriteAction.run(myRootProvider::fireRootSetChanged);
    for (Project project : ProjectManager.getInstance().getOpenProjects()) {
      WriteAction.run(() -> {
        RootsChangeRescanningInfo info = BuildableRootsChangeRescanningInfo.newInstance().addSdk(this).buildInfo();
        ((ProjectRootManagerEx)ProjectRootManager.getInstance(project)).makeRootsChange(EmptyRunnable.getInstance(), info);
      });
    }
  }

  @Override
  public boolean isWritable() {
    return true;
  }

  @NotNull
  @Override
  public RootProvider getRootProvider() {
    return myRootProvider;
  }

  private static void throwReadOnly() {
    throw new IncorrectOperationException("Can't modify, MockJDK is read-only, consider calling .clone() first");
  }

  @Nullable
  @Override
  public <T> T getUserData(@NotNull Key<T> key) {
    return null;
  }

  @Override
  public <T> void putUserData(@NotNull Key<T> key, @Nullable T value) {
    throwReadOnly();
  }

  @Override
  public String toString() {
    return "MockSDK[" + myName + "]";
  }

  private final class MyRootProvider extends RootProviderBaseImpl implements Supplier<Sdk> {
    
    @Override
    public String @NotNull [] getUrls(@NotNull OrderRootType rootType) {
      return ContainerUtil.map2Array(getFiles(rootType), String.class, VirtualFile::getUrl);
    }

    @Override
    public VirtualFile @NotNull [] getFiles(@NotNull OrderRootType rootType) {
      return getRoots(rootType);
    }

    @Override
    public Sdk get() {
      //noinspection TestOnlyProblems
      return MockSdk.this;
    }

    @Override
    public void fireRootSetChanged() {
      super.fireRootSetChanged();
    }
  }
}
