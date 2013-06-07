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
package mrpg.export;

import java.io.ByteArrayOutputStream;
import java.io.File;

import com.flagstone.transform.DefineData;
import com.flagstone.transform.DefineTag;
import com.flagstone.transform.Movie;
import com.flagstone.transform.MovieTag;
import com.flagstone.transform.ShowFrame;
import com.flagstone.transform.SymbolClass;

public class SWFExport extends Export {
	private Movie movie; private int id; private SymbolClass bitmaps, sounds, bytearrays; private File f;
	public SWFExport(File swf) throws Exception {
		f = swf; movie = new Movie(); movie.decodeFromFile(f);
		bitmaps = new SymbolClass(); sounds = new SymbolClass(); bytearrays = new SymbolClass();
		id = 1; for(MovieTag t : movie.getObjects()){
			if(t instanceof DefineTag) id = Math.max(id, ((DefineTag)t).getIdentifier());
		}
	}
	public void addImage(Graphic b, long i, long modified) throws Exception {
		String type = "Ai"+Long.toHexString(i); movie.add(b.defineImage(id)); bitmaps.add(id, type); id++;
	}
	public void addSound(Sound s, long i, long modified) throws Exception {
		String type = "As"+Long.toHexString(i); movie.add(s.getSound(id)); sounds.add(id, type); id++;
	}
	public void addData(ByteArrayOutputStream ar, String t, long i, long modified) throws Exception {
		String type = "A"+t+Long.toHexString(i); movie.add(new DefineData(id, ar.toByteArray())); bytearrays.add(id, type); id++;
	}
	public void finish() throws Exception {
		movie.add(bitmaps); movie.add(sounds); movie.add(bytearrays); movie.add(ShowFrame.getInstance()); movie.encodeToFile(f);
		//NOTE: Uncomment to view exported tags. for(MovieTag tag : movie.getObjects()) System.out.println(tag);
	}
}