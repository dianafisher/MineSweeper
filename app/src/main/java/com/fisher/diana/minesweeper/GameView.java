package com.fisher.diana.minesweeper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.v4.view.GestureDetectorCompat;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * TODO: document your custom view class.
 */
public class GameView extends View {

    private enum CellState {
        UNVISITED,
        EMPTY,
        FLAGGED,
        NUMBERED,
        BOMB,
    }

    private static int GRID_SIZE = 9;
    private static int BOMB_COUNT = 10;

    private Paint mLinePaint;
    private Paint mUnvisitedCellPaint;
    private Paint mEmptyCellPaint;
    private Paint mFlaggedCellPaint;
    private Paint mNumberedCellPaint;
    private Paint mBombCellPaint;
    private Paint mTextPaint;

    private Cell[][] mCellGrid;

    private GestureDetector mGestureDetector;

    public GameView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public GameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {

        mLinePaint = new Paint();
        mLinePaint.setColor(0xFFFFFFFF);

        mUnvisitedCellPaint = new Paint();
        mUnvisitedCellPaint.setColor(0xFFEEEEEE);

        mEmptyCellPaint = new Paint();
        mEmptyCellPaint.setColor(0xFFFEFEFE);

        mFlaggedCellPaint = new Paint();
        mFlaggedCellPaint.setColor(0xFFFFFF00);

        mNumberedCellPaint = new Paint();
        mNumberedCellPaint.setColor(0xFFFEFEFE);

        mBombCellPaint = new Paint();
        mBombCellPaint.setColor(0xFFFF0000);

        mTextPaint = new Paint();
        mTextPaint.setColor(0xFF000000);
        mTextPaint.setTextSize(25);

        mCellGrid = new Cell[GRID_SIZE][GRID_SIZE];
        for( int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                mCellGrid[i][j] = new Cell(i, j);
            }
        }

        // Randomly set BOMB_COUNT bombs in the grid.
        Random random = new Random();
        int bombsPlaced = 0;

        while (bombsPlaced < BOMB_COUNT) {
            int r = random.nextInt(GRID_SIZE);
            int c = random.nextInt(GRID_SIZE);

            if (!mCellGrid[r][c].hasBomb) {
                mCellGrid[r][c].hasBomb = true;
                bombsPlaced++;
            }
        }

        // Calculate the bomb count numbers for each cell not containing a bomb.
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (!mCellGrid[i][j].hasBomb) {
                    mCellGrid[i][j].bombNeighborCount = nearbyBombCount(i, j);
                }
            }
        }

        TapGestureListener gestureListener = new TapGestureListener();
        mGestureDetector = new GestureDetector(context, gestureListener);
        mGestureDetector.setOnDoubleTapListener(gestureListener);
    }

    private int nearbyBombCount(int row, int column) {
        // Scan 8 nearby cells in the grid.

        // Up (row-1)
        // Down (row+1)
        // Left (column-1)
        // Right (column+1)

        int bombCount = 0;

        for (int i = row-1; i <= row+1; i++) {
            for (int j = column-1; j <= column+1; j++) {
                if (hasBomb(i, j)) {
                    bombCount++;
                }
            }
        }
        return bombCount;
    }

    private boolean hasBomb(int row, int column) {
        if (row < 0 || row >= GRID_SIZE) return false;
        if (column < 0 || column >= GRID_SIZE) return false;

        return mCellGrid[row][column].hasBomb;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw grey background
        canvas.drawRGB(200, 200, 200);

        int width = getWidth();
        int height = getHeight();

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Paint paint;
                switch(mCellGrid[r][c].cellState) {
                    case EMPTY:
                        paint = mEmptyCellPaint;
                        break;
                    case BOMB:
                        paint = mBombCellPaint;
                        break;
                    case NUMBERED:
                        paint = mNumberedCellPaint;
                        break;
                    case FLAGGED:
                        paint = mFlaggedCellPaint;
                        break;
                    default:
                        paint = mUnvisitedCellPaint;
                        break;
                }
//                // TODO remove this if statement!
//                if (mCellGrid[r][c].hasBomb) {
//                    paint = mBombCellPaint;
//                }
                canvas.drawRect(c*width/GRID_SIZE, r*height/GRID_SIZE, c*width/GRID_SIZE + width/GRID_SIZE, r*height/GRID_SIZE + height/GRID_SIZE, paint);
                if (mCellGrid[r][c].cellState == CellState.NUMBERED) {
                    canvas.drawText(String.valueOf(mCellGrid[r][c].bombNeighborCount), c * width / GRID_SIZE + 20, r * height / GRID_SIZE + 50, mTextPaint);
                }
            }
        }

        // Draw lines for grid.
        for (int x = 0; x < width; x++) {
            canvas.drawLine(x, 0, x, height, mLinePaint);
            x += width/GRID_SIZE;
        }
        for (int y = 0; y < height; y++) {
            canvas.drawLine(0, y, width, y, mLinePaint);
            y += height/GRID_SIZE;
        }

    }

    public boolean onTouchEvent(MotionEvent event) {
        // forward touch event to our gesture detector
        return mGestureDetector.onTouchEvent(event);
    }

    class TapGestureListener extends GestureDetector.SimpleOnGestureListener {
        private static final String DEBUG_TAG = "TapGestures";

        public boolean onDown(MotionEvent event) {
//            Log.d(DEBUG_TAG, "onDown: " + event.toString());
            return true;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            int[] location = locationFromMotionEvent(event);
            int row = location[0];
            int column = location[1];

//            Log.d("GRID_VIEW", "touch event on cell " + row + "," + column);

            if (mCellGrid[row][column].cellState == CellState.FLAGGED) {
                mCellGrid[row][column].cellState = CellState.UNVISITED;
            } else if (mCellGrid[row][column].cellState == CellState.UNVISITED) {
                mCellGrid[row][column].cellState = CellState.FLAGGED;
            }

            invalidate();

        }

        private int[] locationFromMotionEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            int width = getWidth();
            int height = getHeight();

            // Which cell did they touch?
            int column = (int)(x / (width/GRID_SIZE));
            int row = (int)(y / (height/GRID_SIZE));

            return new int[]{row, column};
        }

        public boolean onSingleTapUp(MotionEvent event) {
//            Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());

            int[] location = locationFromMotionEvent(event);
            int row = location[0];
            int column = location[1];

            if (mCellGrid[row][column].hasBomb) {
                mCellGrid[row][column].cellState = CellState.BOMB;
            } else if (mCellGrid[row][column].bombNeighborCount == 0) {
                // expand until we hit non-empty cells.
                expandFromCell(row, column);
            } else {
                // reveal the number
                mCellGrid[row][column].cellState = CellState.NUMBERED;
            }

            invalidate();

            return true;
        }

        private void expandFromCell(int row, int column) {
//            Log.d(DEBUG_TAG, "checking cell " + row + "," + column);
            if (row < 0 || row >= GRID_SIZE || column < 0 || column >= GRID_SIZE) return;
            if (mCellGrid[row][column].bombNeighborCount > 0) {
                mCellGrid[row][column].cellState = CellState.NUMBERED;
                return;
            }
            if (mCellGrid[row][column].cellState == CellState.EMPTY) return;

            mCellGrid[row][column].cellState = CellState.EMPTY;

            expandFromCell(row+1, column);
            expandFromCell(row+1, column-1);
            expandFromCell(row+1, column+1);
            expandFromCell(row, column-1);
            expandFromCell(row, column+1);
            expandFromCell(row-1, column);
            expandFromCell(row-1, column-1);
            expandFromCell(row-1, column+1);


        }

        private void expandFromCellNR(int row, int column) {
            if (row < 0 || row >= GRID_SIZE || column == 0 || column >= GRID_SIZE) return;

            // Create a queue


        }

//        public boolean onSingleTapConfirmed(MotionEvent event) {
//
//            Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
//
//            float x = event.getX();
//            float y = event.getY();
//
//            int width = getWidth();
//            int height = getHeight();
//
//            // Which cell did they touch?
//            int column = (int)(x / (width/5));
//            int row = (int)(y / (height/5));
//
//            Log.d("GRID_VIEW", "touch event on cell " + row + "," + column);
//
//            mGrid[row][column] = 1;
//
//            invalidate();
//
//            return true;
//        }
    }

    class Cell {
        int row;
        int column;
        boolean visited;
        int bombNeighborCount;
        boolean hasBomb;
        CellState cellState;

        Cell(int row, int column) {
            this.row = row;
            this.column = column;
            this.visited = false;
            this.hasBomb = false;
            this.bombNeighborCount = 0;
            this.cellState = CellState.UNVISITED;
        }
    }
}