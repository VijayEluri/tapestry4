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

package org.apache.tapestry.multipart;

import org.apache.tapestry.BaseComponentTestCase;
import static org.easymock.EasyMock.expect;
import org.testng.annotations.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests for {@link org.apache.tapestry.multipart.UploadFormParametersWrapper}.
 * 
 * @author Howard M. Lewis Ship
 * @since 4.0
 */
@Test
public class TestUploadFormParametersWrapper extends BaseComponentTestCase
{
    private HttpServletRequest newHttpRequest()
    {
        HttpServletRequest req = newMock(HttpServletRequest.class);
        expect(req.getParameterMap()).andReturn( new HashMap() ).anyTimes();
        return req;
    }
    
    public void testMapIsNotModifiable()
    {
        HttpServletRequest request = newHttpRequest();

        replay();

        Map map = new HashMap();

        map.put("key", new String[] { "value" });

        HttpServletRequest r = new UploadFormParametersWrapper(request, map);

        Map pmap = r.getParameterMap();

        assertNotSame(map, pmap);

        assertSame(map.get("key"), pmap.get("key"));

        try
        {
            pmap.put("foo", "bar");
            unreachable();
        }
        catch (UnsupportedOperationException ex)
        {
            // Expected.
        }

        verify();
    }

    public void testGetSingleParameterWhenNull()
    {
        HttpServletRequest request = newHttpRequest();

        replay();

        HttpServletRequest r = new UploadFormParametersWrapper(request, new HashMap());

        assertNull(r.getParameter("unknown-key"));

        verify();
    }

    public void testGetSingleParameterWhenEmptyArray()
    {
        HttpServletRequest request = newHttpRequest();

        replay();

        HashMap params = new HashMap();

        params.put("empty-key", new String[0]);

        HttpServletRequest r = new UploadFormParametersWrapper(request, params);

        assertNull(r.getParameter("empty-key"));

        verify();
    }

    public void testGetParameterValues()
    {
        String[] values =
        { "fred", "barney" };

        HttpServletRequest request = newHttpRequest();

        replay();

        HashMap params = new HashMap();

        params.put("key", values);

        HttpServletRequest r = new UploadFormParametersWrapper(request, params);

        assertSame(values, r.getParameterValues("key"));

        verify();
    }

    public void testGetParameterNames()
    {
        HttpServletRequest request = newHttpRequest();

        replay();

        HashMap params = new HashMap();

        params.put("key", new String[0]);

        HttpServletRequest r = new UploadFormParametersWrapper(request, params);

        Enumeration e = r.getParameterNames();

        assertEquals(true, e.hasMoreElements());
        assertEquals("key", e.nextElement());
        assertEquals(false, e.hasMoreElements());

        verify();
    }
    
    public void testGetUrlParameter()  // Test fix for TAPESTRY-340
    {
        HttpServletRequest req = newMock(HttpServletRequest.class);
        expect(req.getParameterMap()).andReturn(new HashMap(){{put("urlParam", new String[]{"urlParamValue"} );}} ).anyTimes();

        replay();
        
        HttpServletRequest r = new UploadFormParametersWrapper(req, new HashMap());
        
        assertEquals("urlParamValue", r.getParameter("urlParam") );
        assertEquals("urlParam", r.getParameterNames().nextElement() );
    }
}