// Copyright 2005 The Apache Software Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.apache.tapestry.annotations;

import java.lang.reflect.Method;

import org.apache.hivemind.Location;
import org.apache.hivemind.Resource;
import org.apache.tapestry.enhance.EnhancementOperation;
import org.apache.tapestry.spec.ComponentSpecification;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.apache.tapestry.annotations.AssetAnnotationWorker}.
 * 
 */
@Test
public class TestAssetAnnotationWorker extends BaseAnnotationTestCase
{
    public void test_Success()
    {
        Location l = newLocation();
        Resource r = newMock(Resource.class);
        EnhancementOperation op = newOp();
        IComponentSpecification spec = new ComponentSpecification();
        spec.setSpecificationLocation(r);

        replay();

        Method m = findMethod(AnnotatedPage.class, "getGlobalStylesheet");

        new AssetAnnotationWorker().performEnhancement(op, spec, m, l);

        verify();

        IAssetSpecification as = spec.getAsset("globalStylesheet");

        assertEquals("/style/global.css", as.getPath());
        assertEquals(as.getLocation(), l);
        assertEquals("globalStylesheet", as.getPropertyName());
    }

    public void test_Class_Relative_Asset()
    {
        Location l = newLocation();
        Resource r = newMock(Resource.class);
        EnhancementOperation op = newOp();
        IComponentSpecification spec = new ComponentSpecification();
        spec.setSpecificationLocation(r);

        replay();

        Method m = findMethod(AnnotatedPage.class, "getTextAsset");

        new AssetAnnotationWorker().performEnhancement(op, spec, m, l);

        verify();

        IAssetSpecification as = spec.getAsset("textAsset");

        assertEquals("images/test-asset.txt", as.getPath());
        assertEquals(as.getLocation(), l);
        assertEquals("textAsset", as.getPropertyName());
    }
}
