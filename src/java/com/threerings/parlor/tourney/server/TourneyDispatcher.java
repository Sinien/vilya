//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/vilya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.parlor.tourney.server;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

import com.threerings.parlor.tourney.data.TourneyMarshaller;

/**
 * Dispatches requests to the {@link TourneyProvider}.
 */
public class TourneyDispatcher extends InvocationDispatcher<TourneyMarshaller>
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public TourneyDispatcher (TourneyProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public TourneyMarshaller createMarshaller ()
    {
        return new TourneyMarshaller();
    }

    @Override
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case TourneyMarshaller.CANCEL:
            ((TourneyProvider)provider).cancel(
                source, (InvocationService.ConfirmListener)args[0]
            );
            return;

        case TourneyMarshaller.JOIN:
            ((TourneyProvider)provider).join(
                source, (InvocationService.ConfirmListener)args[0]
            );
            return;

        case TourneyMarshaller.LEAVE:
            ((TourneyProvider)provider).leave(
                source, (InvocationService.ConfirmListener)args[0]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
            return;
        }
    }
}
