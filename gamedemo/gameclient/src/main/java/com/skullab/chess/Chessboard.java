/*
 * Copyright (c) 2011 Skullab software @ Ivan Maruca
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.skullab.chess;

import java.util.ArrayList;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.example.uiservice.gameclient.R;
import com.example.uiservice.spi.Position;

/**
 * Displays a chessboard with (or without) notation around the board.
 * @author skullab.com
 * @attr rel R.styleable.Chessaboard_enableNotation
 * @attr rel R.styleable.Chessboard_whiteCellsColor 
 * @attr rel R.styleable.Chessboard_blackCellsColor
 * @attr rel R.styleable.Chessboard_whiteCellsBackground
 * @attr rel R.styleable.Chessboard_blackCellsBackground
 * @attr rel R.styleable.Chessboard_cellsSize
 * @attr rel R.styleable.Chessboard_notationTextSize
 * @attr rel R.styleable.Chessboard_notationTextColor
 * @attr rel R.styleable.Chessboard_notationBackground
 * @attr rel R.styleable.Chessboard_notationTypeface
 */
public class Chessboard extends LinearLayout {
	
	//private static final String TAG = Chessboard.class.getSimpleName() ;
	
	private LayoutInflater layoutInflater ;
	private View chessboard ;
	private OnCellClickListener mListener ;
	
	private String[] cellsName = {
			"a1","b1","c1","d1","e1","f1","g1","h1",
			"a2","b2","c2","d2","e2","f2","g2","h2",
			"a3","b3","c3","d3","e3","f3","g3","h3",
			"a4","b4","c4","d4","e4","f4","g4","h4",
			"a5","b5","c5","d5","e5","f5","g5","h5",
			"a6","b6","c6","d6","e6","f6","g6","h6",
			"a7","b7","c7","d7","e7","f7","g7","h7",
			"a8","b8","c8","d8","e8","f8","g8","h8"
	};
	private int[] notationId = {
			R.id.notation_1_left,
			R.id.notation_1_right,
			R.id.notation_2_left,
			R.id.notation_2_right,
			R.id.notation_3_left,
			R.id.notation_3_right,
			R.id.notation_4_left,
			R.id.notation_4_right,
			R.id.notation_5_left,
			R.id.notation_5_right,
			R.id.notation_6_left,
			R.id.notation_6_right,
			R.id.notation_7_left,
			R.id.notation_7_right,
			R.id.notation_8_left,
			R.id.notation_8_right,
			R.id.notation_up,
			R.id.notation_down
	} ;
	private int[] cellsId = {
		R.id.a1,
		R.id.b1,
		R.id.c1,
		R.id.d1,
		R.id.e1,
		R.id.f1,
		R.id.g1,
		R.id.h1,
		R.id.a2,
		R.id.b2,
		R.id.c2,
		R.id.d2,
		R.id.e2,
		R.id.f2,
		R.id.g2,
		R.id.h2,
		R.id.a3,
		R.id.b3,
		R.id.c3,
		R.id.d3,
		R.id.e3,
		R.id.f3,
		R.id.g3,
		R.id.h3,
		R.id.a4,
		R.id.b4,
		R.id.c4,
		R.id.d4,
		R.id.e4,
		R.id.f4,
		R.id.g4,
		R.id.h4,
		R.id.a5,
		R.id.b5,
		R.id.c5,
		R.id.d5,
		R.id.e5,
		R.id.f5,
		R.id.g5,
		R.id.h5,
		R.id.a6,
		R.id.b6,
		R.id.c6,
		R.id.d6,
		R.id.e6,
		R.id.f6,
		R.id.g6,
		R.id.h6,
		R.id.a7,
		R.id.b7,
		R.id.c7,
		R.id.d7,
		R.id.e7,
		R.id.f7,
		R.id.g7,
		R.id.h7,
		R.id.a8,
		R.id.b8,
		R.id.c8,
		R.id.d8,
		R.id.e8,
		R.id.f8,
		R.id.g8,
		R.id.h8
	};
	
	private ArrayList<Integer> whiteCellsId = new ArrayList<Integer>();
	private ArrayList<Integer> blackCellsId = new ArrayList<Integer>();
	
	public Chessboard(Context context) {
		super(context);
		constructor();
	}

	public Chessboard(Context context, AttributeSet attrs) {
		super(context, attrs);
		constructor();
		styleable(attrs);
	}
	
	// Convenience constructor
	private void constructor(){
		
		layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		chessboard = layoutInflater.inflate(R.layout.chessboard_layout,null);
		
		boolean firstWhite = true ;
		for(int i=0 ; i<cellsId.length ; i++){
			// new line
			if(i % 8 == 0){
				firstWhite = firstWhite ? false : true ;  
			}
			//even
			if(i % 2 == 0){
				if(!firstWhite){
					blackCellsId.add(cellsId[i]);
				}else {
					blackCellsId.add(cellsId[i+1]);
				}
			// odd
			}else{
				if(!firstWhite){
					whiteCellsId.add(cellsId[i]);
				}else {
					whiteCellsId.add(cellsId[i-1]);
				}
			}
			
		}
		
		addView(chessboard);
	}
	// Set style on component (XML attributes)
	private void styleable(AttributeSet attrs){
		
		TypedArray t = getContext().obtainStyledAttributes(attrs,R.styleable.Chessboard);
		
		boolean enable_notation = t.getBoolean(R.styleable.Chessboard_enableNotation, true);
		int cells_size = (int)t.getDimension(R.styleable.Chessboard_cellsSize,35);
		float text_size = t.getDimension(R.styleable.Chessboard_notationTextSize,12);
		int text_color = t.getColor(R.styleable.Chessboard_notationTextColor,Color.WHITE);
		String text_typeface = t.getString(R.styleable.Chessboard_notationTypeface);
		
		if(text_typeface != null){
			AssetManager am = this.getResources().getAssets();
			Typeface type = Typeface.createFromAsset(am,text_typeface);
			setNotationTypeface(type);
		}
		/*@deprecated
		int white_cells_color = t.getColor(R.styleable.Chessboard_whiteCellsColor,R.color.white);
		int black_cells_color = t.getColor(R.styleable.Chessboard_blackCellsColor,R.color.black);
		setWhiteCellsColor(white_cells_color);
		setBlackCellsColor(black_cells_color);*/
		
		// white cells background
		Drawable w_c_b = t.getDrawable(R.styleable.Chessboard_whiteCellsBackground);
		if(w_c_b != null){
			setWhiteCellsBackground(w_c_b);
		}else{
			int white_color = t.getColor(R.styleable.Chessboard_whiteCellsBackground, Color.WHITE);
			setWhiteCellsBackground(white_color);
		}
		// black cells background
		Drawable b_c_b = t.getDrawable(R.styleable.Chessboard_blackCellsBackground);
		if(b_c_b != null){
			setBlackCellsBackground(b_c_b);
		}else{
			int black_color = t.getColor(R.styleable.Chessboard_blackCellsBackground, Color.BLACK);
			setBlackCellsBackground(black_color);
		}
		// notation background
		Drawable notation_drawable = t.getDrawable(R.styleable.Chessboard_notationBackground);
		if(notation_drawable != null){
			setNotationBackground(notation_drawable);
		}else{
			int notation_color = t.getColor(R.styleable.Chessboard_notationBackground,Color.BLACK);
			setNotationBackgroundColor(notation_color);
		}
		
		enableNotation(enable_notation);
		setCellsSize(cells_size);
		setNotationTextSize(text_size);
		setNotationTextColor(text_color);
	}
	/**
	 * Enables or not the display notation. By default the notation is always displayed.
	 * @param enable true to display otherwise false.
	 */
	public void enableNotation(boolean enable){
		for(int id : notationId){
			View v = chessboard.findViewById(id);
			v.setVisibility(enable ? View.VISIBLE : View.INVISIBLE);
		}
	}
    
	public void setWhiteCellsColor(int color){
		for(int id : whiteCellsId){
			View v = chessboard.findViewById(id);
			v.setBackgroundColor(color);
		}
	}

    public void clearWhiteCells(int color){
        for(int id : whiteCellsId){
            View v = chessboard.findViewById(id);
            v.setBackgroundColor(color);
            String notation = getContext().getResources().getResourceEntryName(v.getId());
            removeDrawableOnCell(notation);
        }
    }

    public void clearBlackCells(int color){
        for(int id : blackCellsId){
            View v = chessboard.findViewById(id);
            v.setBackgroundColor(color);
            String notation = getContext().getResources().getResourceEntryName(v.getId());
            removeDrawableOnCell(notation);
        }
    }

	public void setBlackCellsColor(int color){
		for(int id : blackCellsId){
			View v = chessboard.findViewById(id);
			v.setBackgroundColor(color);
		}
	}

	/**
	 * Sets the "white cells" background color
	 * @param color
	 */
	public void setWhiteCellsBackground(int color){
		for(int id : whiteCellsId){
			View v = chessboard.findViewById(id);
			v.setBackgroundColor(color);
		}
	}

	/**
	 * Sets the "white cells" background drawable
	 * @param d drawable
	 */
	public void setWhiteCellsBackground(Drawable d){
		for(int id : whiteCellsId){
			View v = chessboard.findViewById(id);
			v.setBackgroundDrawable(d);
		}
	}
	/**
	 * Sets the "black cells" background color
	 * @param color
	 */
	public void setBlackCellsBackground(int color){
		for(int id : blackCellsId){
			View v = chessboard.findViewById(id);
			v.setBackgroundColor(color);
		}
	}
	/**
	 * Sets the "black cells" background drawable
	 * @param d drawable
	 */
	public void setBlackCellsBackground(Drawable d){
		for(int id : blackCellsId){
			View v = chessboard.findViewById(id);
			v.setBackgroundDrawable(d);
		}
	}
	/**
	 * Sets the background color of notation
	 * @param color
	 */
	public void setNotationBackgroundColor(int color){
		for(int id : notationId){
			View v = chessboard.findViewById(id);
			v.setBackgroundColor(color);
		}
	}
	/**
	 * Sets the background image of notation
	 * @param d
	 */
	public void setNotationBackground(Drawable d){
		for(int id : notationId){
			View v = chessboard.findViewById(id);
			v.setBackgroundDrawable(d);
		}
	}
	/**
	 * Sets the background image of notation
	 * @param resId
	 */
	public void setNotationBackground(int resId){
		for(int id : notationId){
			View v = chessboard.findViewById(id);
			v.setBackgroundResource(resId);
		}
	}
	/**
	 * Sets the text color of notation
	 * @param color
	 */
	public void setNotationTextColor(int color){
		for(int id : notationId){
			View v = chessboard.findViewById(id);
			if(id == R.id.notation_up || id == R.id.notation_down){
				for(int i = 0 ; i < 10 ; i++){
					TextView tv = (TextView)((TableRow)v).getChildAt(i);
					tv.setTextColor(color);
				}
			}else{
				((TextView)v).setTextColor(color);
			}
		}
	}
	/**
	 * Sets the text sixe of notation
	 * @param size
	 */
	public void setNotationTextSize(float size){
		for(int id : notationId){
			View v = chessboard.findViewById(id);
			if(id == R.id.notation_up || id == R.id.notation_down){
				for(int i = 0 ; i < 10 ; i++){
					TextView tv = (TextView)((TableRow)v).getChildAt(i);
					tv.setTextSize(size);
				}
			}else{
				((TextView)v).setTextSize(size);
			}
		}
	}
	/**
	 * Sets the text typeface of notation
	 * @param type
	 */
	public void setNotationTypeface(Typeface type){
		for(int id : notationId){
			View v = chessboard.findViewById(id);
			if(id == R.id.notation_up || id == R.id.notation_down){
				for(int i = 0 ; i < 10 ; i++){
					TextView tv = (TextView)((TableRow)v).getChildAt(i);
					tv.setTypeface(type);
				}
			}else{
				((TextView)v).setTypeface(type);
			}
		}
	}
	/**
	 * Gets the text size of notation
	 * @return the size
	 */
	public float getNotationTextSize(){
		TextView tv = (TextView)chessboard.findViewById(notationId[0]);
		return tv.getTextSize();
	}

	public ImageView getViewFromNotation(String notation){
		ImageView v = null ;
		for(int i=0 ; i<cellsName.length;i++){
			if(cellsName[i].equals(notation)){
				v = (ImageView)chessboard.findViewById(cellsId[i]);
				break;
			}
		}
		return v ;
	}

	/**
	 * This method allow to retrieve the image drawable of a cell by notation.
	 * The notation must be a string that represents the coordinate of the cell, for example "e5"
	 * @param notation The coordinate of the cell
	 * @return The view's image or null if no drawable has been assigned
	 */
	public Drawable getDrawableOnCell(String notation){
		ImageView v = getViewFromNotation(notation);
		return v.getDrawable() ;
	}

	public void setDrawableOnCell(Position position, ColorDrawable drawable) {
		ImageView v = getViewFromNotation(("" + position.getX()) + position.getY());
		v.setImageDrawable(drawable);
	}

	/**
	 * Allows to remove a drawable on a cell.
	 * @param notation The coordinate of the cell
	 */
	public void removeDrawableOnCell(String notation){
		ImageView v = getViewFromNotation(notation);
		v.setImageDrawable(null);
	}
	/**
	 * Sets the cell size.
	 * @param s size
	 */
	public void setCellsSize(int s){
		for(int id : cellsId){
			ImageView v = (ImageView)chessboard.findViewById(id);
			ViewGroup.LayoutParams lp = v.getLayoutParams();
			lp.width = s ;
			lp.height = s ;
			v.setLayoutParams(lp);
		}
	}
	/**
	 * Allows to set the {@link OnCellClickListener} for this Chessboard
	 * @param listener
	 */
	public void setOnCellClickListener(OnCellClickListener listener){
		mListener = listener ;
		for(int id : cellsId){
			View v = chessboard.findViewById(id);
			v.setOnClickListener(new View.OnClickListener() {
				
                public void onClick(View v) {
					String notation = getContext().getResources().getResourceEntryName(v.getId());
                    String x = notation.substring(0, 1);
                    String y = notation.substring(1, 2);
                    if(mListener != null)mListener.onCellClick(new Position(x.charAt(0), Integer.parseInt(y)));
				}
			});
		}
	}

    public void clearAll() {
        clearWhiteCells(Color.WHITE);
        clearBlackCells(Color.BLACK);
    }

    public void setDefaultDrawableOnCell(Position position) {
        final String notation = ("" + position.getX()) + position.getY();
        removeDrawableOnCell(notation);
    }

    /**
	 * Interface definition for a callback to be invoked when a cell is clicked. 
	 * @author skullab.com
	 *
	 */
	public interface OnCellClickListener {
		public void onCellClick(Position position);
	}
}
