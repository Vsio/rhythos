/*******************************************************************************
 * Rhythos Editor is a game editor and project management tool for making RPGs on top of the Rhythos Game system.
 * 
 * Copyright (C) 2013  David Maletz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package mrpg.editor.resource;

import mrpg.editor.MapEditor;


public class Workspace extends Folder.Root {
	private static final long serialVersionUID = -781096720788373713L;
	public Workspace(String n, MapEditor e){super(n, e);}
	public boolean canAdd(byte t, Object key){return t == Type.PROJECT;}
	
	public int getProjectCount(){return getChildCount();}
	public Project getProject(int i){return (Project)getChild(i);}
	public Resource copy(){return new Workspace(getName(), editor).copyChildren(this);}
}
