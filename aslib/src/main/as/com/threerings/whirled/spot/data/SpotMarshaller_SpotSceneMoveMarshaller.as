//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/vilya/
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

package com.threerings.whirled.spot.data {

import com.threerings.io.TypedArray;

import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.spot.client.SpotService_SpotSceneMoveListener;

/**
 * Marshalls instances of the SpotService_SpotSceneMoveMarshaller interface.
 */
public class SpotMarshaller_SpotSceneMoveMarshaller
    extends InvocationMarshaller_ListenerMarshaller
{
    /** The method id used to dispatch <code>moveRequiresServerSwitch</code> responses. */
    public static const MOVE_REQUIRES_SERVER_SWITCH :int = 1;

    /** The method id used to dispatch <code>moveSucceeded</code> responses. */
    public static const MOVE_SUCCEEDED :int = 2;

    /** The method id used to dispatch <code>moveSucceededWithScene</code> responses. */
    public static const MOVE_SUCCEEDED_WITH_SCENE :int = 3;

    /** The method id used to dispatch <code>moveSucceededWithUpdates</code> responses. */
    public static const MOVE_SUCCEEDED_WITH_UPDATES :int = 4;

    /** The method id used to dispatch <code>requestCancelled</code> responses. */
    public static const REQUEST_CANCELLED :int = 5;

    // from InvocationMarshaller_ListenerMarshaller
    override public function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case MOVE_REQUIRES_SERVER_SWITCH:
            (listener as SpotService_SpotSceneMoveListener).moveRequiresServerSwitch(
                (args[0] as String), (args[1] as TypedArray /* of int */));
            return;

        case MOVE_SUCCEEDED:
            (listener as SpotService_SpotSceneMoveListener).moveSucceeded(
                (args[0] as int), (args[1] as PlaceConfig));
            return;

        case MOVE_SUCCEEDED_WITH_SCENE:
            (listener as SpotService_SpotSceneMoveListener).moveSucceededWithScene(
                (args[0] as int), (args[1] as PlaceConfig), (args[2] as SceneModel));
            return;

        case MOVE_SUCCEEDED_WITH_UPDATES:
            (listener as SpotService_SpotSceneMoveListener).moveSucceededWithUpdates(
                (args[0] as int), (args[1] as PlaceConfig), (args[2] as TypedArray /* of class com.threerings.whirled.data.SceneUpdate */));
            return;

        case REQUEST_CANCELLED:
            (listener as SpotService_SpotSceneMoveListener).requestCancelled(
                );
            return;

        default:
            super.dispatchResponse(methodId, args);
            return;
        }
    }
}
}
