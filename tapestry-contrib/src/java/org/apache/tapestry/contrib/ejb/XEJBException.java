// Copyright 2004, 2005 The Apache Software Foundation
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

package org.apache.tapestry.contrib.ejb;

import javax.ejb.EJBException;

/**
 *  Extended version of {@link EJBException} that includes a root cause.
 *  {@link EJBException} doesn't have quite the right constructor for this ...
 *  it has an equivalent to the rootCause property, (causedByException), but
 *  doesn't have a constructor that allows us to set a custom message.
 *
 *  @author Howard Lewis Ship
 *
 **/

public class XEJBException extends EJBException
{
    private static final long serialVersionUID = 3712108893575174833L;
    
    private final Throwable _rootCause;

    public XEJBException(String message)
    {
        super(message);
        _rootCause = null;
    }

    public XEJBException(String message, Throwable rootCause)
    {
        super(message);

        this._rootCause = rootCause;
    }

    public XEJBException(Throwable rootCause)
    {
        super(rootCause.getMessage());

        this._rootCause = rootCause;
    }

    public Throwable getRootCause()
    {
        return _rootCause;
    }
}
