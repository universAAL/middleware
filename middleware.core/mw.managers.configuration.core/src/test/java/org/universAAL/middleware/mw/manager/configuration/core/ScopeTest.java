/*******************************************************************************
 * Copyright 2014 Universidad Politécnica de Madrid
 * Copyright 2014 Fraunhofer-Gesellschaft - Institute for Computer Graphics Research
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.universAAL.middleware.mw.manager.configuration.core;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.universAAL.middleware.mw.manager.configuration.core.impl.factories.ScopeFactory;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.scope.AALSpaceScope;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.scope.AppPartScope;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.scope.ApplicationScope;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.scope.InstanceScope;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.scope.ModuleScope;
import org.universAAL.middleware.mw.manager.configuration.core.interfaces.scope.Scope;

/**
 * @author amedrano
 *
 */
public class ScopeTest{
    

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsAALSpace1(){
	new AALSpaceScope(null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsAALSpace2(){
	new AALSpaceScope("");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsAALSpace3(){
	new AALSpaceScope("not:ok");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsAALSpace4(){
	new AALSpaceScope("not ok");
    }
    @Test
    public void testConstructorsAALSpace5(){
	new AALSpaceScope("ok");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsInstance1(){
	new InstanceScope("ok", (String) null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsInstance2(){
	new InstanceScope("ok", "");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsInstance3(){
	new InstanceScope("ok", "not:ok");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsInstance4(){
	new InstanceScope("ok", "not ok");
    }
    @Test
    public void testConstructorsInstance5(){
	new InstanceScope("ok", "ok");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsModule1(){
	new ModuleScope("ok", "peer",(String) null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsModule2(){
	new ModuleScope("ok", "peer",(String) null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsModule3(){
	new ModuleScope("ok", "peer","");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsModule4(){
	new ModuleScope("ok", "peer","not:ok");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsModule5(){
	new ModuleScope("ok", "peer","not ok");
    }
    @Test
    public void testConstructorsModule6(){
	new ModuleScope("ok", "peer","ok");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsApp1(){
	new ApplicationScope("ok", (String) null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsApp2(){
	new ApplicationScope("ok", "");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsApp3(){
	new ApplicationScope("ok", "not:ok");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsApp4(){
	new ApplicationScope("ok", "not ok");
    }
    @Test
    public void testConstructorsApp5(){
	new ApplicationScope("ok", "app");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsAppPart1(){
	new AppPartScope("ok", "app", (String) null);
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsAppPart2(){
	new AppPartScope("ok", "app", "");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsAppPart3(){
	new AppPartScope("ok", "app", "not:ok");
    }
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorsAppPart4(){
	new AppPartScope("ok", "app", "not ok");
    }
    @Test
    public void testConstructorsAppPart5(){
	new AppPartScope("ok", "app", "ok");
    }
    
    @Test
    public void testFactory(){
	testconv(new AALSpaceScope("id"));
	testconv(new InstanceScope("id", "peerId"));
	testconv(new ModuleScope("id", "peerId", "moduleID"));
	testconv(new ApplicationScope("id", "appID"));
	testconv(new AppPartScope("id", "appID", "partID"));
	
    }
    
    private void testconv(Scope s){
	String urn = ScopeFactory.getScopeURN(s);
	System.out.println(urn);
	assertEquals(urn,ScopeFactory.getScopeURN(ScopeFactory.getScope(urn)));
    }
}
