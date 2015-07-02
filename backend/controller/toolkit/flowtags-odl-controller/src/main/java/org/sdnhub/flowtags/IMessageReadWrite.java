
/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.sdnhub.flowtags;

import java.util.List;

/**
 * This interface defines low level routines to read/write messages on an open
 * socket channel. If secure communication is desired, these methods also perform
 * encryption and decryption of the network data.
 */
public interface IMessageReadWrite {
        /**
         * Sends the OF message out over the socket channel. For secure
         * communication, the data will be encrypted.
         *
         * @param msg OF message to be sent
         * @throws Exception
         */
        public void asyncSend(FTMessage msg) throws Exception;

        /**
         * Resumes sending the remaining messages in the outgoing buffer
         * @throws Exception
         */
        public void resumeSend() throws Exception;

        /**
         * Reads the incoming network data from the socket and retrieves the OF
         * messages. For secure communication, the data will be decrypted first.
         *
         * @return list of OF messages
         * @throws Exception
         */
    public List<FTMessage> readMessages() throws Exception;
    
    public List<PSIMessage> readPSIMessages() throws Exception;

        /**
         * Proper clean up when the switch connection is closed
         *
         * @return
         * @throws Exception
         */
    public void stop() throws Exception;
}
