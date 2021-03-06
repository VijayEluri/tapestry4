// Copyright 2006 The Apache Software Foundation
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

package org.apache.tapestry.binding;

import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.hivemind.Location;
import org.apache.tapestry.IComponent;
import org.apache.tapestry.coerce.ValueConverter;
import static org.easymock.EasyMock.expect;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.apache.tapestry.binding.ClientIdBinding}. 
 */
@Test
public class TestClientIdBinding extends BindingTestCase
{
    public void test_Get_Object()
    {
        IComponent nested = newComponent();
        IComponent component = newMock(IComponent.class);
        
        expect(component.getComponent("foo")).andReturn(nested);
        expect(nested.getClientId()).andReturn("id");

        ValueConverter vc = newValueConverter();

        Location l = newLocation();

        replay();

        ClientIdBinding b = new ClientIdBinding("param", vc, l, component, "foo");

        assertSame(component, b.getComponent());
        assertSame("id", b.getObject());

        verify();
    }

    public void test_Get_Object_Failure()
    {
        IComponent component = newMock(IComponent.class);

        Throwable t = new ApplicationRuntimeException("No such component.");

        expect(component.getComponent("foo")).andThrow(t);

        ValueConverter vc = newValueConverter();

        Location l = newLocation();

        replay();

        ClientIdBinding b = new ClientIdBinding("param", vc, l, component, "foo");

        try
        {
            b.getObject();
            unreachable();
        }
        catch (ApplicationRuntimeException ex)
        {
            assertEquals(t.getMessage(), ex.getMessage());
            assertSame(t, ex.getRootCause());
            assertSame(l, ex.getLocation());
        }

        verify();
    }
}
