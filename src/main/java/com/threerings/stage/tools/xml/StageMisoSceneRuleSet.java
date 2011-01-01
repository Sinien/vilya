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

package com.threerings.stage.tools.xml;

import com.threerings.miso.data.SparseMisoSceneModel;
import com.threerings.miso.tools.xml.SparseMisoSceneRuleSet;

import com.threerings.stage.data.StageMisoSceneModel;

/**
 * Customizes the miso scene rule set such that it creates {@link
 * StageMisoSceneModel} instances.
 */
public class StageMisoSceneRuleSet extends SparseMisoSceneRuleSet
{
    @Override
    protected SparseMisoSceneModel createMisoSceneModel ()
    {
        return new StageMisoSceneModel();
    }
}
