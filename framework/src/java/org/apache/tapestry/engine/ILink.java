// Copyright 2004 The Apache Software Foundation
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

package org.apache.tapestry.engine;

/**
 * Define a link that may be generated as part of a page render. The vast majority of links are tied
 * to {@link IEngineService services}and are, in fact, callbacks. A small number, such as those
 * generated by {@link org.apache.tapestry.link.GenericLink}component, are to arbitrary locations.
 * In addition, ILink differentiates between the path portion of the link, and any query parameters
 * encoded into a link, primarily to benefit {@link org.apache.tapestry.form.Form}, which needs to
 * encode the query parameters as hidden form fields.
 * <p>
 * In addition, an ILink is responsible for passing constructed URLs through
 * {@link org.apache.tapestry.IRequestCycle#encodeURL(String)}as needed.
 * 
 * @author Howard Lewis Ship
 * @since 3.0
 */

public interface ILink
{
    /**
     * Returns the relative URL as a String. A relative URL may include a leading slash, but omits
     * the scheme, host and port portions of a full URL.
     * 
     * @return the relative URL, with no anchor, but including query parameters.
     */

    public String getURL();

    /**
     * Returns the relative URL as a String. This is used for most links.
     * 
     * @param anchor
     *            if not null, appended to the URL
     * @param includeParameters
     *            if true, parameters are included
     */

    public String getURL(String anchor, boolean includeParameters);

    /**
     * Returns the absolute URL as a String, using default scheme, server and port, including
     * parameters, and no anchor.
     */

    public String getAbsoluteURL();

    /**
     * Returns the absolute URL as a String.
     * 
     * @param scheme
     *            if not null, overrides the default scheme.
     * @param server
     *            if not null, overrides the default server
     * @param port
     *            if non-zero, overrides the default port
     * @param anchor
     *            if not null, appended to the URL
     * @param includeParameters
     *            if true, parameters are included
     */

    public String getAbsoluteURL(String scheme, String server, int port, String anchor,
            boolean includeParameters);

    /**
     * Returns an array of parameters names (in no alphabetical order).
     * 
     * @see #getParameterValues(String)
     */

    public String[] getParameterNames();

    /**
     * Returns the values for the named parameter. Will return null if the no value is defined for
     * the parameter.
     */

    public String[] getParameterValues(String name);
}