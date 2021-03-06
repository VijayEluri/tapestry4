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

package org.apache.tapestry.integration.app1.pages;

/**
 * Integration test for TAPESTRY-1141.
 */
public abstract class Tap1141 extends Home {        
    public abstract String getText();
    public abstract void setText(String text);
    public abstract String getNum();
    public abstract void setNum(String num);    
    
    public void doProcess()
    {
        setText(getNum());
    } 
}
