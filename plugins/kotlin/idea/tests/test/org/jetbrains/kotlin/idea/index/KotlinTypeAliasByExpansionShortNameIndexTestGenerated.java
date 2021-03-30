/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.idea.index;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.jetbrains.kotlin.test.TestRoot;
import org.junit.runner.RunWith;

/*
 * This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}.
 * DO NOT MODIFY MANUALLY.
 */
@SuppressWarnings("all")
@TestRoot("idea/tests")
@TestDataPath("$CONTENT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
@TestMetadata("testData/typealiasExpansionIndex")
public class KotlinTypeAliasByExpansionShortNameIndexTestGenerated extends AbstractKotlinTypeAliasByExpansionShortNameIndexTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
    }

    @TestMetadata("functionalTypes.kt")
    public void testFunctionalTypes() throws Exception {
        runTest("testData/typealiasExpansionIndex/functionalTypes.kt");
    }

    @TestMetadata("generics.kt")
    public void testGenerics() throws Exception {
        runTest("testData/typealiasExpansionIndex/generics.kt");
    }

    @TestMetadata("simpleType.kt")
    public void testSimpleType() throws Exception {
        runTest("testData/typealiasExpansionIndex/simpleType.kt");
    }
}
