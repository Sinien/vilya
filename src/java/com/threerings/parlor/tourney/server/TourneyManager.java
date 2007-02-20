//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

import com.samskivert.util.Interval;
import com.samskivert.util.ResultListener;

import com.threerings.util.Name;

import com.threerings.crowd.data.BodyObject;

import com.threerings.parlor.tourney.data.Participant;
import com.threerings.parlor.tourney.data.TourneyConfig;
import com.threerings.parlor.tourney.data.TourneyCodes;
import com.threerings.parlor.tourney.data.TourneyMarshaller;
import com.threerings.parlor.tourney.data.TourneyObject;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;
import com.threerings.presents.server.PresentsDObjectMgr;
import com.threerings.presents.data.ClientObject;

/**
 * Controls a running tourney.
 */
public abstract class TourneyManager
    implements TourneyProvider, TourneyCodes
{
    public TourneyManager (TourneyConfig config, TourniesManager tmgr, Comparable key,
            InvocationService.ResultListener listener)
    {
        _config = config;
        _tmgr = tmgr;
        _key = key;

        // creare and configure our Tourney object
        _trobj = getOMgr().registerObject(new TourneyObject());
        _trobj.setTourneyService((TourneyMarshaller)getInvMgr().registerDispatcher(
                    new TourneyDispatcher(this)));

        _trobj.config = config;

        // if we've got logic data already then we must be resuming a persisted tournament
        if (config.logic != null) {
            _trobj.state = TourneyObject.PAUSED;
        } else {
            _trobj.startsIn = config.startsIn;
            // keep track of when we start in millis
            _startTime = System.currentTimeMillis() + (MINUTE * _config.startsIn);
        }

        if (listener != null) {
            listener.requestProcessed(Integer.valueOf(_trobj.getOid()));
        }
    }

    // documentation inherited from TourneyProvider
    public void cancel (ClientObject caller, InvocationService.ConfirmListener listener)
        throws InvocationException
    {
        // don't allow cancelation of a running tourney
        if (_trobj.state != TourneyObject.PENDING) {
            throw new InvocationException(ALREADY_IN_PROGRESS);

        // or a tourney that already has participants
        } else if (_trobj.participants.size() != 0) {
            throw new InvocationException(HAS_PLAYERS);

        } else {
            cancelTourney(CANCELLED);
        }
    }

    // documentation inherited from TourneyProvider
    public void join (ClientObject caller, final InvocationService.ConfirmListener listener)
        throws InvocationException
    {
        BodyObject body = (BodyObject)caller;

        // make sure the tourney hasn't already started
        if (_trobj.state != TourneyObject.PENDING) {
            throw new InvocationException(TOO_LATE);
        }

        // make sure they haven't already signed up
        final Participant part = makeParticipant(body);
        if (_trobj.participants.contains(part)) {
            throw new InvocationException(ALREADY_IN_TOURNEY);
        }

        // allow the implementing class to do further join checks
        joinTourney(body);

        // check the entrance fee
        if (_trobj.config.entryFee != null) {
            if (!_trobj.config.entryFee.hasFee(body)) {
                listener.requestFailed("FAILED_ENTRY_FEE");
                return;
            }

            // make the assumption that they're going to get in
            _trobj.addToParticipants(part);

            ResultListener rl = new ResultListener() {
                public void requestCompleted (Object result) {
                    listener.requestProcessed();
                }
                public void requestFailed (Exception cause) {
                    // remove them from the tourney
                    _trobj.removeFromParticipants(part.getKey());
                    listener.requestFailed("FAILED_ENTRY_FEE");
                }
            };

            _trobj.config.entryFee.reserveFee(body, rl);

        } else {
            _trobj.addToParticipants(part);
            listener.requestProcessed();
        }
    }

    // documentation inherited from TourneyProvider
    public void leave (ClientObject caller, InvocationService.ConfirmListener listener)
        throws InvocationException
    {
        BodyObject body = (BodyObject)caller;

        // can't leave unless the tourney is just pending
        if (_trobj.state != TourneyObject.PENDING) {
            throw new InvocationException(TOO_LATE_LEAVE);
        }

        Comparable key = body.username;
        if (!_trobj.participants.containsKey(key)) {
            throw new InvocationException(NOT_IN_TOURNEY);
        }

        // remove them IMMEDIATELY from the participation list
        _trobj.removeFromParticipants(key);

        // return the entry fee
        if (_trobj.config.entryFee != null) {
            _trobj.config.entryFee.returnFee(body);
        }

        listener.requestProcessed();
    }

    /**
     * Cancel the tourney, return entry fees to all participants.
     */
    public void cancelTourney (String cause)
    {
        if (isFinished()) {
            // show's over, nothing to see here
            return;
        }

        _trobj.setState(TourneyObject.CANCELLED);

        notifyAllParticipants(cause);

        // return the fees
        if (_trobj.config.entryFee != null) {
            for (Participant part : _trobj.participants) {
                BodyObject body = lookupBody(part.username);
                if (body != null) {
                    _trobj.config.entryFee.returnFee(body);
                }
            }
        }

        releaseTourney();
    }

    /**
     * Returns true if the tourney is finished.
     */
    public boolean isFinished ()
    {
        return _trobj == null ? false :
            _trobj.state == TourneyObject.FINISHED || _trobj.state == TourneyObject.CANCELLED;
    }

    /**
     * Returns true if the tourney is pending.
     */
    public boolean isPending ()
    {
        return _trobj == null ? false : _trobj.state == TourneyObject.PENDING;
    }

    /**
     * Returns true if the tourney is running.
     */
    public boolean isRunning ()
    {
        return _trobj == null ? false : _trobj.state == TourneyObject.RUNNING;
    }

    /**
     * Returns true if the tourney is paused.
     */
    public boolean isPaused ()
    {
        return _trobj == null ? false : _trobj.state == TourneyObject.PAUSED;
    }

    /**
     * Returns true if it is time or past time the tourney starts.
     */
    public boolean shouldStart (long now)
    {
        return _trobj == null ? false : _startTime <= now;
    }

    /** Creates a {@link Participant} record for the specified user. */
    protected Participant makeParticipant (BodyObject body)
    {
        Participant part = new Participant();
        part.username = body.username;
        return part;
    }

    public abstract void notifyAllParticipants (String msg);

    /**
     * Releases the tourney from the tourney manager, and removes the tourney from the list of
     * tournies.
     */
    protected void releaseTourney ()
    {
        _tmgr.releaseTourney(_key);

        // release the tourney provider
        if (_trobj.tourneyService != null) {
            getInvMgr().clearDispatcher(_trobj.tourneyService);
            _trobj.tourneyService = null;
        }

        // destroy the object in a couple of minutes
        new Interval(getOMgr()) {
            public void expired () {
                getOMgr().destroyObject(_trobj.getOid());
            }
        }.schedule(MINUTE * 2);
    }

    /**
     * Returns a reference to our distributed object manager.
     */
    protected abstract PresentsDObjectMgr getOMgr ();

    /**
     * Returns a reference to our invocation manager.
     */
    protected abstract InvocationManager getInvMgr ();

    /**
     * Looks up the BodyObject for a username.
     */
    protected abstract BodyObject lookupBody (Name username);

    /**
     * Will throw an InvocationException if the user cannot join the tourney.
     */
    protected abstract void joinTourney (BodyObject body)
        throws InvocationException;

    /** One minute in milliseconds. */
    protected static long MINUTE = 60 * 1000L;

    /** Our touney configuration. */
    protected TourneyConfig _config;

    /** Our distributed tourney object. */
    protected TourneyObject _trobj;

    /** Reference to the tournies manager. */
    protected TourniesManager _tmgr;

    /** The time, in milliseconds, when the tourney starts. */
    protected long _startTime;

    /** The key this tourney is recorded under. */
    protected Comparable _key;
}