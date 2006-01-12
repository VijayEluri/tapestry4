// Copyright 2004, 2005, 2006 The Apache Software Foundation
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

import org.apache.hivemind.Messages;
import org.apache.hivemind.impl.MessageFormatter;
import org.apache.tapestry.IBinding;
import org.apache.tapestry.IComponent;

/**
 * @author Howard M. Lewis Ship
 * @since 4.0
 */
final class BindingMessages
{

    private final static Messages MESSAGES = new MessageFormatter(BindingMessages.class);

    /** @since 4.1 */
    private BindingMessages()
    {
    }

    static String convertObjectError(IBinding binding, Throwable cause)
    {
        return MESSAGES.format("convert-object-error", binding.getDescription(), cause);
    }

    static String readOnlyBinding(IBinding binding)
    {
        return MESSAGES.format("read-only-binding", binding.getDescription(), binding);
    }

    static String missingAsset(IComponent component, String assetName)
    {
        return MESSAGES.format("missing-asset", component.getExtendedId(), assetName);
    }

    static String listenerMethodFailure(IComponent component, String methodName, Throwable cause)
    {
        return MESSAGES.format("listener-method-failure", methodName, component.getExtendedId(), cause);
    }
}
