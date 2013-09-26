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

package org.universAAL.middleware.aalspace.json.model;

import java.io.Serializable;

import org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor;



/**
 * 
 * 
 * @author <a href="mailto:sterfano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 * @since 2.0.1
 */
public class ChannelDescriptor
    implements Serializable, IChannelDescriptor
{

    /**
     * 
     */
    private static final long serialVersionUID = 2164969851491853720L;
    protected String channelName;
    protected String channelURL;
    protected String channelValue;

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#getChannelName()
     */
    public String getChannelName() {
        return channelName;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#setChannelName(java.lang.String)
     */
    public void setChannelName(String value) {
        this.channelName = value;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#isSetChannelName()
     */
    public boolean isSetChannelName() {
        return (this.channelName!= null);
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#getChannelURL()
     */
    public String getChannelURL() {
        return channelURL;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#setChannelURL(java.lang.String)
     */
    public void setChannelURL(String value) {
        this.channelURL = value;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#isSetChannelURL()
     */
    public boolean isSetChannelURL() {
        return (this.channelURL!= null);
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#getChannelValue()
     */
    public String getChannelValue() {
        return channelValue;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#setChannelValue(java.lang.String)
     */
    public void setChannelValue(String value) {
        this.channelValue = value;
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor#isSetChannelValue()
     */
    public boolean isSetChannelValue() {
        return (this.channelValue!= null);
    }

}
