//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.whirled.client {

import com.threerings.presents.dobj.MessageAdapter;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;

import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.util.WhirledContext;

/**
 * The base scene controller class. It is expected that users of the
 * Whirled services will extend this controller class when creating
 * specialized controllers for their scenes.
 */
public /*abstract*/ class SceneController extends PlaceController
{
    // documentation inherited
    override public function init (ctx :CrowdContext, config :PlaceConfig) :void
    {
        super.init(ctx, config);
        _wctx = WhirledContext(ctx);
    }

    // documentation inherited
    override public function willEnterPlace (plobj :PlaceObject) :void
    {
        super.willEnterPlace(plobj);
        plobj.addListener(_updateListener);
    }

    // documentation inherited
    override public function didLeavePlace (plobj :PlaceObject) :void
    {
        super.didLeavePlace(plobj);
        plobj.removeListener(_updateListener);
    }

    /**
     * This method is called if a scene update is recorded while we
     * currently occupy a scene. The default implementation will update
     * our local scene and scene model, but derived classes will likely
     * want to ensure that the update is properly displayed.
     */
    protected function sceneUpdated (update :SceneUpdate) :void
    {
        // apply the update to the scene
        _wctx.getSceneDirector().updateReceived(update);
    }

    /** Used to listen for scene updates. */
    protected var _updateListener :MessageListener = new MessageAdapter(
        function (event :MessageEvent) :void {
            if (event.getName() == SceneCodes.SCENE_UPDATE) {
                sceneUpdated(event.getArgs()[0] as SceneUpdate);
            }
        }
    );

    protected var _wctx :WhirledContext;
}
}
