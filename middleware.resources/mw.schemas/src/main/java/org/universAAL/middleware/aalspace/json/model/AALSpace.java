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
import java.util.ArrayList;
import java.util.List;

import org.universAAL.middleware.interfaces.aalspace.model.IAALSpace;
import org.universAAL.middleware.interfaces.aalspace.model.IChannelDescriptor;
import org.universAAL.middleware.interfaces.aalspace.model.ICommunicationChannels;
import org.universAAL.middleware.interfaces.aalspace.model.IPeeringChannel;
import org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor;

/**
 *
 *
 * @author <a href="mailto:sterfano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @version $LastChangedRevision$ ($LastChangedDate$)
 * @since 2.0.1
 */
public class AALSpace implements Serializable, IAALSpace {

    public static class CommunicationChannels implements Serializable,
            ICommunicationChannels {

        /**
         *
         */
        private static final long serialVersionUID = 7742876482751281317L;
        protected List<ChannelDescriptor> channelDescriptor;

        /*
         * (non-Javadoc)
         *
         * @see org.universAAL.middleware.interfaces.aalspace.model.
         * ICommunicationChannels#getChannelDescriptor()
         */
        public List<IChannelDescriptor> getChannelDescriptor() {
            if (channelDescriptor == null) {
                channelDescriptor = new ArrayList<ChannelDescriptor>();
            }
            return new ArrayList<IChannelDescriptor>(channelDescriptor);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.universAAL.middleware.interfaces.aalspace.model.
         * ICommunicationChannels#isSetChannelDescriptor()
         */
        public boolean isSetChannelDescriptor() {
            return ((this.channelDescriptor != null) && (!this.channelDescriptor
                    .isEmpty()));
        }

        /*
         * (non-Javadoc)
         *
         * @see org.universAAL.middleware.interfaces.aalspace.model.
         * ICommunicationChannels#unsetChannelDescriptor()
         */
        public void unsetChannelDescriptor() {
            this.channelDescriptor = null;
        }

    }

    public static class PeeringChannel implements Serializable, IPeeringChannel {

        /**
         *
         */
        private static final long serialVersionUID = -6845397412316320965L;
        protected IChannelDescriptor channelDescriptor;

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.IPeeringChannel
         * #getChannelDescriptor()
         */
        public IChannelDescriptor getChannelDescriptor() {
            return channelDescriptor;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.IPeeringChannel
         * #setChannelDescriptor
         * (org.universAAL.middleware.interfaces.aalspace.model
         * .ChannelDescriptor)
         */
        public void setChannelDescriptor(IChannelDescriptor value) {
            this.channelDescriptor = value;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.IPeeringChannel
         * #isSetChannelDescriptor()
         */
        public boolean isSetChannelDescriptor() {
            return (this.channelDescriptor != null);
        }

    }

    public static class SpaceDescriptor implements Serializable,
            ISpaceDescriptor {

        /**
         *
         */
        private static final long serialVersionUID = 3779806454200564931L;
        protected String profile;
        protected String spaceId;
        protected String spaceName;
        protected String spaceDescription;
        protected String spaceCoordinator;

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor
         * #getProfile()
         */
        public String getProfile() {
            return profile;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor
         * #setProfile(java.lang.String)
         */
        public void setProfile(String value) {
            this.profile = value;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor
         * #isSetProfile()
         */
        public boolean isSetProfile() {
            return (this.profile != null);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor
         * #getSpaceId()
         */
        public String getSpaceId() {
            return spaceId;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor
         * #setSpaceId(java.lang.String)
         */
        public void setSpaceId(String value) {
            this.spaceId = value;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor
         * #isSetSpaceId()
         */
        public boolean isSetSpaceId() {
            return (this.spaceId != null);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor
         * #getSpaceName()
         */
        public String getSpaceName() {
            return spaceName;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor
         * #setSpaceName(java.lang.String)
         */
        public void setSpaceName(String value) {
            this.spaceName = value;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor
         * #isSetSpaceName()
         */
        public boolean isSetSpaceName() {
            return (this.spaceName != null);
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor
         * #getSpaceDescription()
         */
        public String getSpaceDescription() {
            return spaceDescription;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor
         * #setSpaceDescription(java.lang.String)
         */
        public void setSpaceDescription(String value) {
            this.spaceDescription = value;
        }

        /*
         * (non-Javadoc)
         *
         * @see
         * org.universAAL.middleware.interfaces.aalspace.model.ISpaceDescriptor
         * #isSetSpaceDescription()
         */
        public boolean isSetSpaceDescription() {
            return (this.spaceDescription != null);
        }

		public String getSpaceCoordinator() {
		
			return spaceCoordinator;
		}



    }

    /**
     *
     */
    private static final long serialVersionUID = -1679644587451410869L;
    protected ISpaceDescriptor spaceDescriptor;
    protected IPeeringChannel peeringChannel;
    protected ICommunicationChannels communicationChannels;
    protected String security;

    /**
     * Gets the value of the spaceDescriptor property.
     *
     * @return possible object is {@link AALSpace.SpaceDescriptor }
     *
     */
    public ISpaceDescriptor getSpaceDescriptor() {
        return spaceDescriptor;
    }

    /**
     * Sets the value of the spaceDescriptor property.
     *
     * @param value
     *            allowed object is {@link AALSpace.SpaceDescriptor }
     *
     */
    public void setSpaceDescriptor(ISpaceDescriptor value) {
        this.spaceDescriptor = value;
    }

    public boolean isSetSpaceDescriptor() {
        return (this.spaceDescriptor != null);
    }

    /**
     * Gets the value of the peeringChannel property.
     *
     * @return possible object is {@link AALSpace.PeeringChannel }
     *
     */
    public IPeeringChannel getPeeringChannel() {
        return peeringChannel;
    }

    /**
     * Sets the value of the peeringChannel property.
     *
     * @param value
     *            allowed object is {@link AALSpace.PeeringChannel }
     *
     */
    public void setPeeringChannel(IPeeringChannel value) {
        this.peeringChannel = value;
    }

    public boolean isSetPeeringChannel() {
        return (this.peeringChannel != null);
    }

    /**
     * Gets the value of the communicationChannels property.
     *
     * @return possible object is {@link AALSpace.CommunicationChannels }
     *
     */
    public ICommunicationChannels getCommunicationChannels() {
        return communicationChannels;
    }

    /**
     * Sets the value of the communicationChannels property.
     *
     * @param value
     *            allowed object is {@link AALSpace.CommunicationChannels }
     *
     */
    public void setCommunicationChannels(ICommunicationChannels value) {
        this.communicationChannels = value;
    }

    public boolean isSetCommunicationChannels() {
        return (this.communicationChannels != null);
    }

    /**
     * Gets the value of the security property.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSecurity() {
        return security;
    }

    /**
     * Sets the value of the security property.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setSecurity(String value) {
        this.security = value;
    }

    public boolean isSetSecurity() {
        return (this.security != null);
    }


}
