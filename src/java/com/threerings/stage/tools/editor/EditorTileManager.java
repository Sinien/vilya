//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.stage.tools.editor;

import com.samskivert.util.HashIntMap;

import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.TileSet;
import com.threerings.media.image.ImageManager;

import com.threerings.miso.tile.MisoTileManager;
import com.threerings.resource.ResourceManager;

/**
 * Extends the normal Miso Tile Manager to allow the use of a set of test tiles that can be used in
 *  the editor.  These are loaded through the TestTileLoader.
 */
public class EditorTileManager extends MisoTileManager
{
    public EditorTileManager (ResourceManager rmgr, ImageManager imgr)
    {
        super(rmgr, imgr);
    }

    @Override //Documentation inherited
    public TileSet getTileSet (int tileSetId)
        throws NoSuchTileSetException
    {
        TileSet testSet = _testSets.get(tileSetId);
        if (testSet != null) {
            return testSet;
        } else {
            return super.getTileSet(tileSetId);
        }
    }

    /**
     * Add a test tile set to search before normal tilesets.
     */
    public void addTestTileSet (int id, TileSet set)
    {
        _testSets.put(id, set);
    }

    /**
     * Clear all of our existing test tile sets.
     */
    public void clearTestTileSets ()
    {
        _testSets.clear();
    }

    /**
     * All the test tile sets we have available.
     */
    protected HashIntMap<TileSet> _testSets = new HashIntMap<TileSet>();
}
