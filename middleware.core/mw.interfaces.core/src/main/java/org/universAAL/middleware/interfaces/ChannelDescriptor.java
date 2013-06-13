/*
        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */

package org.universAAL.middleware.interfaces;

import java.io.Serializable;

/**
 * This class describes a communication channel
 * 
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:francesco.furfari@isti.cnr.it">Francesco Furfari</a>
 */
public class ChannelDescriptor implements Serializable {

    private static final long serialVersionUID = 1339517729177690537L;
    private String channelName;
    private String channelDescriptorFileURL;

    public String getChannelName() {
	return channelName;
    }

    public void setChannelName(String channelName) {
	this.channelName = channelName;
    }

    public String getChannelDescriptorFileURL() {
	return channelDescriptorFileURL;
    }

    public void setChannelDescriptorFileURL(String channelDescriptorFileURL) {
	this.channelDescriptorFileURL = channelDescriptorFileURL;
    }

    public String getChannelValue() {
	return channelValue;
    }

    public void setChannelValue(String channelValue) {
	this.channelValue = channelValue;
    }

    private String channelValue;

    public ChannelDescriptor(String name, String descriptor, String value) {
	this.channelName = name;
	this.channelDescriptorFileURL = descriptor;
	this.channelValue = value;
    }

}
