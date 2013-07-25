/*
 * Copyright (C) 2013 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview
 * This file represents a TicTacToe board object, with all needed drawing and
 * state update functions.
 */

STATE = {
  EMPTY: 0,
  CROSS: 1,
  NAUGHT: 2
};

GAME_RESULT = {
  X_WON: 'X-won',
  O_WON: 'O-won',
  DRAW: 'draw',
  ABANDONED: 'abandoned'
};

WINNING_LOCATION = {
  ROW_0: 0,
  ROW_1: 1,
  ROW_2: 2,
  COLUMN_0: 3,
  COLUMN_1: 4,
  COLUMN_2: 5,
  DIAGONAL_TOPLEFT: 6,
  DIAGONAL_BOTTOMLEFT: 7
};

/**
 * Creates an empty board object with no location
 * @param {CanvasRenderingContext2D} context the 2D context of the canvas that
 *     the board is drawn on.
 * @constructor
 */
function board(context) {
  this.mContext = context;
  this.mGameResult = -1;
  this.mWinningLocation = -1;
  this.mBoard = new Array(3);
  for (var i = 0; i < this.mBoard.length; i++) {
    this.mBoard[i] = new Array(3);
    for (var j = 0; j < this.mBoard[0].length; j++) {
      this.mBoard[i][j] = STATE.EMPTY;
    }
  }
}

/**
 * Resets the board to a starting state.
 * @this {board}
 */
function boardReset() {
  this.mGameResult = -1;
  this.mWinningLocation = -1;
  this.mContext.beginPath();
  this.mContext.clearRect(this.X, this.Y, this.mContext.canvas.width,
      this.mContext.canvas.height);
  this.drawGrid();
  for (var i = 0; i < this.mBoard.length; i++) {
    for (var j = 0; j < this.mBoard[0].length; j++) {
      this.mBoard[i][j] = STATE.EMPTY;
    }
  }
}

/**
 * Calculates and sets internally the board's width, height, x, and y.
 * @this {board}
 */
function boardCalcDimensions() {
  if (this.mContext.canvas.width > this.mContext.canvas.height) {
    this.dimension = this.mContext.canvas.height - 2 * this.margin;
  } else {
    this.dimension = this.mContext.canvas.width - 2 * this.margin;
  }
  this.X = (this.mContext.canvas.width - this.dimension) / 2;
  this.Y = (this.mContext.canvas.height - this.dimension) / 2;
  this.cellWidth = this.dimension / 3;
}

/**
 * Calculates board dimensions.
 * @this {board}
 */
function boardClear() {
  this.calcDimensions();
}

/**
 * Draws the hashmark-shaped grid for TicTacToe.
 * @this {board}
 */
function boardDrawGrid() {
  // draw background
  this.mContext.fillStyle = '#BDBDBD';
  this.mContext.strokeStyle = '#000000';
  this.mContext.fillRect(this.X, this.Y, this.dimension, this.dimension);
  // draw grid
  this.mContext.lineWidth = 5;
  this.mContext.moveTo(this.X + this.cellWidth, this.Y);
  this.mContext.lineTo(this.X + this.cellWidth, this.Y + this.dimension);
  this.mContext.stroke();
  this.mContext.moveTo(this.X + this.cellWidth * 2, this.Y);
  this.mContext.lineTo(this.X + this.cellWidth * 2, this.Y + this.dimension);
  this.mContext.stroke();
  this.mContext.moveTo(this.X, this.Y + this.cellWidth);
  this.mContext.lineTo(this.X + this.dimension, this.Y + this.cellWidth);
  this.mContext.stroke();
  this.mContext.moveTo(this.X, this.Y + this.cellWidth * 2);
  this.mContext.lineTo(this.X + this.dimension, this.Y + this.cellWidth * 2);
  this.mContext.stroke();
}

/**
 * Draws an O symbol in the given row and column.
 * @param {number} row the row the piece should be placed in.
 * @param {number} col the column the piece should be placed in.
 * @this {board}
 * @return {boolean} true if the selected row and column is a valid square
 *     to put a piece in.
 */
function boardDrawNaught(row, col) {
  if (this.mBoard[row][col] != STATE.EMPTY) {
    console.info('Invalid position: ' + row + ' ' + col +
        ' val:' + this.mBoard[row][col]);
    return false;
  }
  this.mBoard[row][col] = STATE.NAUGHT;
  this.mContext.lineWidth = 8;
  this.mContext.strokeStyle = '#FFFF00';
  this.mContext.beginPath();
  this.mContext.arc(this.X + this.cellWidth * (col + 0.5),
      this.Y + this.cellWidth * (row + 0.5),
      this.cellWidth / 2 - this.pieceMargin, 0, 360);
  this.mContext.stroke();
  return true;
}

/**
 * Draws an X symbol in the given row and column.
 * @param {number} row the row the piece should be placed in.
 * @param {number} col the column the piece should be placed in.
 * @this {board}
 * @return {boolean} true if the selected row and column is a valid square
 *     to put a piece in.
 */
function boardDrawCross(row, col) {
  if (this.mBoard[row][col] != STATE.EMPTY) {
    console.info('Invalid position: ' + row + ' ' + col +
        ' val:' + this.mBoard[row][col]);
    return false;
  }
  this.mBoard[row][col] = STATE.CROSS;
  this.mContext.strokeStyle = '#0000FF';
  this.mContext.lineWidth = 8;
  this.mContext.beginPath();
  this.mContext.moveTo(this.X + this.cellWidth * col + this.pieceMargin,
      this.Y + this.cellWidth * row + this.pieceMargin);
  this.mContext.lineTo(this.X + this.cellWidth * (col + 1) - this.pieceMargin,
      this.Y + this.cellWidth * (row + 1) - this.pieceMargin);
  this.mContext.stroke();
  this.mContext.moveTo(this.X + this.cellWidth * (col + 1) - this.pieceMargin,
      this.Y + this.cellWidth * row + this.pieceMargin);
  this.mContext.lineTo(this.X + this.cellWidth * col + this.pieceMargin,
      this.Y + this.cellWidth * (row + 1) - this.pieceMargin);
  this.mContext.stroke();
  return true;
}

/**
 * Draws the line connecting the winning three pieces.
 * @this {board}
 * @return {boolean} true if the winning three spaces is valid.
 */
function boardDrawWinningLocation() {
  var xStart = yStart = xEnd = yEnd = -1;
  if (this.mWinningLocation == WINNING_LOCATION.ROW_0) {
    xStart = 0.05;
    xEnd = 2.95;
    yStart = yEnd = 0.5;
  } else if (this.mWinningLocation == WINNING_LOCATION.ROW_1) {
    xStart = 0.05;
    xEnd = 2.95;
    yStart = yEnd = 1.5;
  } else if (this.mWinningLocation == WINNING_LOCATION.ROW_2) {
    xStart = 0.05;
    xEnd = 2.95;
    yStart = yEnd = 2.5;
  } else if (this.mWinningLocation == WINNING_LOCATION.COLUMN_0) {
    yStart = 0.05;
    yEnd = 2.95;
    xStart = xEnd = 0.5;
  } else if (this.mWinningLocation == WINNING_LOCATION.COLUMN_1) {
    yStart = 0.05;
    yEnd = 2.95;
    xStart = xEnd = 1.5;
  } else if (this.mWinningLocation == WINNING_LOCATION.COLUMN_2) {
    yStart = 0.05;
    yEnd = 2.95;
    xStart = xEnd = 2.5;
  } else if (this.mWinningLocation == WINNING_LOCATION.DIAGONAL_TOPLEFT) {
    xStart = yStart = 0.05;
    xEnd = yEnd = 2.95;
  } else if (this.mWinningLocation == WINNING_LOCATION.DIAGONAL_BOTTOMLEFT) {
    xStart = yEnd = 2.95;
    yStart = xEnd = 0.05;
  } else {
    console.log('Unknown winning location: ' + this.mWinningLocation);
    return false;
  }
  this.mContext.lineWidth = 10;
  this.mContext.strokeStyle = '#FF0000';
  this.mContext.beginPath();
  this.mContext.moveTo(this.X + this.cellWidth * xStart,
      this.Y + this.cellWidth * yStart);
  this.mContext.lineTo(this.X + this.cellWidth * xEnd,
      this.Y + this.cellWidth * yEnd);
  this.mContext.stroke();
  return true;
}

/**
 * Logs the current state of the board's pieces and results.
 * @this {board}
 */
function boardPrintBoard() {
  for (var i = 0; i < this.mBoard.length; i++) {
    console.log('[ ' + this.mBoard[i][0] + ', ' + this.mBoard[i][1] + ', ' +
                this.mBoard[i][2] + ' ]');
  }
  console.log('gameResult: ' + this.mGameResult);
  console.log('winningLoc: ' + this.mWinningLocation);
}

/**
 * Determines whether the game is over, whether by winning or draw.
 * @this {board}
 * @return {boolean} true if the game ended via a win or a draw.
 */
function boardIsGameOver() {
  var isBoardFull = true;
  this.printBoard();
  // Check the rows
  for (var i = 0; i < this.mBoard.length; i++) {
    if ((this.mBoard[i][0] != STATE.EMPTY) &&
        (this.mBoard[i][1] == this.mBoard[i][0]) &&
        (this.mBoard[i][2] == this.mBoard[i][0])) {
      this.mGameResult = GAME_RESULT.O_WON;
      if (this.mBoard[i][0] == STATE.CROSS) {
        this.mGameResult = GAME_RESULT.X_WON;
      }
      if (i == 0) {
        this.mWinningLocation = WINNING_LOCATION.ROW_0;
      } else if (i == 1) {
        this.mWinningLocation = WINNING_LOCATION.ROW_1;
      } else {
        this.mWinningLocation = WINNING_LOCATION.ROW_2;
      }
    }
    if ((isBoardFull == true) && ((this.mBoard[i][0] == STATE.EMPTY) ||
        (this.mBoard[i][1] == STATE.EMPTY) ||
        (this.mBoard[i][2] == STATE.EMPTY))) {
      isBoardFull = false;
    }
  }
  this.printBoard();

  // Check the columns
  for (var j = 0; j < this.mBoard[0].length; j++) {
    if ((this.mBoard[0][j] != STATE.EMPTY) &&
        (this.mBoard[1][j] == this.mBoard[0][j]) &&
        (this.mBoard[2][j] == this.mBoard[0][j])) {
      this.mGameResult = GAME_RESULT.O_WON;
      if (this.mBoard[0][j] == STATE.CROSS) {
        this.mGameResult = GAME_RESULT.X_WON;
      }
      if (j == 0) {
        this.mWinningLocation = WINNING_LOCATION.COLUMN_0;
      } else if (j == 1) {
        this.mWinningLocation = WINNING_LOCATION.COLUMN_1;
      } else {
        this.mWinningLocation = WINNING_LOCATION.COLUMN_2;
      }
      break;
    }
  }
  this.printBoard();

  // Check diagonals
  if ((this.mBoard[0][0] != STATE.EMPTY) &&
      (this.mBoard[1][1] == this.mBoard[0][0]) &&
      (this.mBoard[2][2] == this.mBoard[0][0])) {
    this.mWinningLocation = WINNING_LOCATION.DIAGONAL_TOPLEFT;
    this.mGameResult = GAME_RESULT.O_WON;
    if (this.mBoard[0][0] == STATE.CROSS) {
      this.mGameResult = GAME_RESULT.X_WON;
    }
  } else if ((this.mBoard[0][2] != STATE.EMPTY) &&
      (this.mBoard[1][1] == this.mBoard[0][2]) &&
      (this.mBoard[2][0] == this.mBoard[0][2])) {
    this.mWinningLocation = WINNING_LOCATION.DIAGONAL_BOTTOMLEFT;
    this.mGameResult = GAME_RESULT.O_WON;
    if (this.mBoard[0][2] == STATE.CROSS) {
      this.mGameResult = GAME_RESULT.X_WON;
    }
  }

  // Check whether the game was won or drawn
  if ((this.mGameResult == GAME_RESULT.X_WON) ||
      (this.mGameResult == GAME_RESULT.O_WON)) {
    this.drawWinningLocation();
    return true;
  }
  if (isBoardFull == true) {
    this.mGameResult = GAME_RESULT.DRAW;
    return true;
  }
  return false;
}

/**
 * Updates this game's result to abandoned.
 * @this {board}
 */
function boardSetGameAbandoned() {
  this.mGameResult = GAME_RESULT.ABANDONED;
}

function boardGetGameResult() {
  return this.mGameResult;
}

function boardGetWinningLocation() {
  return this.mWinningLocation;
}


board.prototype.calcDimensions = boardCalcDimensions;
board.prototype.clear = boardClear;
board.prototype.drawCross = boardDrawCross;
board.prototype.drawGrid = boardDrawGrid;
board.prototype.drawNaught = boardDrawNaught;
board.prototype.drawWinningLocation = boardDrawWinningLocation;
board.prototype.getGameResult = boardGetGameResult;
board.prototype.getWinningLocation = boardGetWinningLocation;
board.prototype.isGameOver = boardIsGameOver;
board.prototype.printBoard = boardPrintBoard;
board.prototype.margin = 50;
board.prototype.pieceMargin = 20;
board.prototype.setGameAbandoned = boardSetGameAbandoned;
board.prototype.reset = boardReset;
board.prototype.GAME_RESULT = GAME_RESULT;
board.prototype.STATE = STATE;
board.prototype.WINNING_LOCATION = WINNING_LOCATION;
