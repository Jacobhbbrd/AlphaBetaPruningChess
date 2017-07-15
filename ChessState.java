import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/*
 * Code for representing state of the chessboard and chess piece moves
 * provided by Dr. Michael Gashler from the University of Arkansas.
 * Code for alpha-beta pruning algorithm and the rest of the code by Jacob Hubbard.
 * Program takes two numbers as command line arguments.
 * Each number represents the depth that will be checked to determine the next move for a player.
 * A command line argument of '0' means the player will be a human player.
 * Any number greater than 0 means the player will be a computer player.
 * So the command line arguments "0 5" means player 1 will be a human player and
 * player 2 will be a computer player that checks 5 moves ahead before determining
 * the best move to take next.
 * Making both numbers the same may result in a never ending game as both
 * computer players will sometimes repeat moves over and over so that neither player loses.
 * The never ending game issue may occassional happen even when the numbers are different.
 * This happens because this program does not account for all of the formal rules of chess 
 * which lets the computer players prolong the game with moves not normally allowed.
 * If both players are computer players, the computer that checks the most moves ahead should
 * usually win.
 */

class ChessState
{
    public static final int MAX_PIECE_MOVES = 27;
    public static final int None = 0;
    public static final int Pawn = 1;
    public static final int Rook = 2;
    public static final int Knight = 3;
    public static final int Bishop = 4;
    public static final int Queen = 5;
    public static final int King = 6;
    public static final int PieceMask = 7;
    public static final int WhiteMask = 8;
    public static final int AllMask = 15;
    public static ChessState.ChessMove moveToMake;
    
    int[] m_rows;
    
    ChessState()
    {
        m_rows = new int[8];
        resetBoard();
    }
    
    ChessState(ChessState that)
    {
        m_rows = new int[8];
        for(int i = 0; i < 8; i++)
            this.m_rows[i] = that.m_rows[i];
    }
    
    int getPiece(int col, int row)
    {
        return (m_rows[row] >> (4 * col)) & PieceMask;
    }
    
    boolean isWhite(int col, int row)
    {
        return (((m_rows[row] >> (4 * col)) & WhiteMask) > 0 ? true : false);
    }
    
    /// Sets the piece at location (col, row). If piece is None, then it doesn't
    /// matter what the value of white is.
    void setPiece(int col, int row, int piece, boolean white)
    {
        m_rows[row] &= (~(AllMask << (4 * col)));
        m_rows[row] |= ((piece | (white ? WhiteMask : 0)) << (4 * col));
    }
    
    /// Sets up the board for a new game
    void resetBoard()
    {
        setPiece(0, 0, Rook, true);
        setPiece(1, 0, Knight, true);
        setPiece(2, 0, Bishop, true);
        setPiece(3, 0, Queen, true);
        setPiece(4, 0, King, true);
        setPiece(5, 0, Bishop, true);
        setPiece(6, 0, Knight, true);
        setPiece(7, 0, Rook, true);
        for(int i = 0; i < 8; i++)
            setPiece(i, 1, Pawn, true);
        for(int j = 2; j < 6; j++)
        {
            for(int i = 0; i < 8; i++)
                setPiece(i, j, None, false);
        }
        for(int i = 0; i < 8; i++)
            setPiece(i, 6, Pawn, false);
        setPiece(0, 7, Rook, false);
        setPiece(1, 7, Knight, false);
        setPiece(2, 7, Bishop, false);
        setPiece(3, 7, Queen, false);
        setPiece(4, 7, King, false);
        setPiece(5, 7, Bishop, false);
        setPiece(6, 7, Knight, false);
        setPiece(7, 7, Rook, false);
    }
    
    /// Positive means white is favored. Negative means black is favored.
    int heuristic(Random rand)
    {
        int score = 0;
        for(int y = 0; y < 8; y++)
        {
            for(int x = 0; x < 8; x++)
            {
                int p = getPiece(x, y);
                int value;
                switch(p)
                {
                    case None: value = 0; break;
                    case Pawn: value = 10; break;
                    case Rook: value = 63; break;
                    case Knight: value = 31; break;
                    case Bishop: value = 36; break;
                    case Queen: value = 88; break;
                    case King: value = 500; break;
                    default: throw new RuntimeException("what?");
                }
                if(isWhite(x, y))
                    score += value;
                else
                    score -= value;
            }
        }
        return score + rand.nextInt(3) - 1;
    }
    
    /// Returns an iterator that iterates over all possible moves for the specified color
    ChessMoveIterator iterator(boolean white)
    {
        return new ChessMoveIterator(this, white);
    }
    
    /// Returns true iff the parameters represent a valid move
    boolean isValidMove(int xSrc, int ySrc, int xDest, int yDest)
    {
        ArrayList<Integer> possible_moves = moves(xSrc, ySrc);
        for(int i = 0; i < possible_moves.size(); i += 2)
        {
            if(possible_moves.get(i).intValue() == xDest && possible_moves.get(i + 1).intValue() == yDest)
                return true;
        }
        return false;
    }
    
    /// Print a representation of the board to the specified stream
    void printBoard(PrintStream stream)
    {
        stream.println("  A  B  C  D  E  F  G  H");
        stream.print(" +");
        for(int i = 0; i < 8; i++)
            stream.print("--+");
        stream.println();
        for(int j = 7; j >= 0; j--)
        {
            stream.print(Character.toString((char)(49 + j)));
            stream.print("|");
            for(int i = 0; i < 8; i++)
            {
                int p = getPiece(i, j);
                if(p != None)
                {
                    if(isWhite(i, j))
                        stream.print("w");
                    else
                        stream.print("b");
                }
                switch(p)
                {
                    case None: stream.print("  "); break;
                    case Pawn: stream.print("p"); break;
                    case Rook: stream.print("r"); break;
                    case Knight: stream.print("n"); break;
                    case Bishop: stream.print("b"); break;
                    case Queen: stream.print("q"); break;
                    case King: stream.print("K"); break;
                    default: stream.print("?"); break;
                }
                stream.print("|");
            }
            stream.print(Character.toString((char)(49 + j)));
            stream.print("\n +");
            for(int i = 0; i < 8; i++)
                stream.print("--+");
            stream.println();
        }
        stream.println("  A  B  C  D  E  F  G  H");
    }
    
    /// Pass in the coordinates of a square with a piece on it
    /// and it will return the places that piece can move to.
    ArrayList<Integer> moves(int col, int row)
    {
        ArrayList<Integer> pOutMoves = new ArrayList<Integer>();
        int p = getPiece(col, row);
        boolean bWhite = isWhite(col, row);
        int nMoves = 0;
        int i, j;
        switch(p)
        {
            case Pawn:
                if(bWhite)
                {
                    if(!checkPawnMove(pOutMoves, col, inc(row), false, bWhite) && row == 1)
                        checkPawnMove(pOutMoves, col, inc(inc(row)), false, bWhite);
                    checkPawnMove(pOutMoves, inc(col), inc(row), true, bWhite);
                    checkPawnMove(pOutMoves, dec(col), inc(row), true, bWhite);
                }
                else
                {
                    if(!checkPawnMove(pOutMoves, col, dec(row), false, bWhite) && row == 6)
                        checkPawnMove(pOutMoves, col, dec(dec(row)), false, bWhite);
                    checkPawnMove(pOutMoves, inc(col), dec(row), true, bWhite);
                    checkPawnMove(pOutMoves, dec(col), dec(row), true, bWhite);
                }
                break;
            case Bishop:
                for(i = inc(col), j=inc(row); true; i = inc(i), j = inc(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                for(i = dec(col), j=inc(row); true; i = dec(i), j = inc(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                for(i = inc(col), j=dec(row); true; i = inc(i), j = dec(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                for(i = dec(col), j=dec(row); true; i = dec(i), j = dec(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                break;
            case Knight:
                checkMove(pOutMoves, inc(inc(col)), inc(row), bWhite);
                checkMove(pOutMoves, inc(col), inc(inc(row)), bWhite);
                checkMove(pOutMoves, dec(col), inc(inc(row)), bWhite);
                checkMove(pOutMoves, dec(dec(col)), inc(row), bWhite);
                checkMove(pOutMoves, dec(dec(col)), dec(row), bWhite);
                checkMove(pOutMoves, dec(col), dec(dec(row)), bWhite);
                checkMove(pOutMoves, inc(col), dec(dec(row)), bWhite);
                checkMove(pOutMoves, inc(inc(col)), dec(row), bWhite);
                break;
            case Rook:
                for(i = inc(col); true; i = inc(i))
                    if(checkMove(pOutMoves, i, row, bWhite))
                        break;
                for(i = dec(col); true; i = dec(i))
                    if(checkMove(pOutMoves, i, row, bWhite))
                        break;
                for(j = inc(row); true; j = inc(j))
                    if(checkMove(pOutMoves, col, j, bWhite))
                        break;
                for(j = dec(row); true; j = dec(j))
                    if(checkMove(pOutMoves, col, j, bWhite))
                        break;
                break;
            case Queen:
                for(i = inc(col); true; i = inc(i))
                    if(checkMove(pOutMoves, i, row, bWhite))
                        break;
                for(i = dec(col); true; i = dec(i))
                    if(checkMove(pOutMoves, i, row, bWhite))
                        break;
                for(j = inc(row); true; j = inc(j))
                    if(checkMove(pOutMoves, col, j, bWhite))
                        break;
                for(j = dec(row); true; j = dec(j))
                    if(checkMove(pOutMoves, col, j, bWhite))
                        break;
                for(i = inc(col), j=inc(row); true; i = inc(i), j = inc(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                for(i = dec(col), j=inc(row); true; i = dec(i), j = inc(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                for(i = inc(col), j=dec(row); true; i = inc(i), j = dec(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                for(i = dec(col), j=dec(row); true; i = dec(i), j = dec(j))
                    if(checkMove(pOutMoves, i, j, bWhite))
                        break;
                break;
            case King:
                checkMove(pOutMoves, inc(col), row, bWhite);
                checkMove(pOutMoves, inc(col), inc(row), bWhite);
                checkMove(pOutMoves, col, inc(row), bWhite);
                checkMove(pOutMoves, dec(col), inc(row), bWhite);
                checkMove(pOutMoves, dec(col), row, bWhite);
                checkMove(pOutMoves, dec(col), dec(row), bWhite);
                checkMove(pOutMoves, col, dec(row), bWhite);
                checkMove(pOutMoves, inc(col), dec(row), bWhite);
                break;
            default:
                break;
        }
        return pOutMoves;
    }
    
    /// Moves the piece from (xSrc, ySrc) to (xDest, yDest). If this move
    /// gets a pawn across the board, it becomes a queen. If this move
    /// takes a king, then it will remove all pieces of the same color as
    /// the king that was taken and return true to indicate that the move
    /// ended the game.
    boolean move(int xSrc, int ySrc, int xDest, int yDest) throws Exception
    {
        if(xSrc < 0 || xSrc >= 8 || ySrc < 0 || ySrc >= 8)
            throw new Exception("out of range");
        if(xDest < 0 || xDest >= 8 || yDest < 0 || yDest >= 8)
            throw new Exception("out of range");
        int target = getPiece(xDest, yDest);
        int p = getPiece(xSrc, ySrc);
        if(p == None)
            throw new Exception("There is no piece in the source location");
        if(target != None && isWhite(xSrc, ySrc) == isWhite(xDest, yDest))
            throw new Exception("It is illegal to take your own piece");
        if(p == Pawn && (yDest == 0 || yDest == 7))
            p = Queen; // a pawn that crosses the board becomes a queen
        boolean white = isWhite(xSrc, ySrc);
        setPiece(xDest, yDest, p, white);
        setPiece(xSrc, ySrc, None, true);
        if(target == King)
        {
            // If you take the opponent's king, remove all of the opponent's pieces. This
            // makes sure that look-ahead strategies don't try to look beyond the end of
            // the game (example: sacrifice a king for a king and some other piece.)
            int x, y;
            for(y = 0; y < 8; y++)
            {
                for(x = 0; x < 8; x++)
                {
                    if(getPiece(x, y) != None)
                    {
                        if(isWhite(x, y) != white)
                        {
                            setPiece(x, y, None, true);
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    static int inc(int pos)
    {
        if(pos < 0 || pos >= 7)
            return -1;
        return pos + 1;
    }
    
    static int dec(int pos)
    {
        if(pos < 1)
            return -1;
        return pos -1;
    }
    
    boolean checkMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bWhite)
    {
        if(col < 0 || row < 0)
            return true;
        int p = getPiece(col, row);
        if(p > 0 && isWhite(col, row) == bWhite)
            return true;
        pOutMoves.add(col);
        pOutMoves.add(row);
        return (p > 0);
    }
    
    boolean checkPawnMove(ArrayList<Integer> pOutMoves, int col, int row, boolean bDiagonal, boolean bWhite)
    {
        if(col < 0 || row < 0)
            return true;
        int p = getPiece(col, row);
        if(bDiagonal)
        {
            if(p == None || isWhite(col, row) == bWhite)
                return true;
        }
        else
        {
            if(p > 0)
                return true;
        }
        pOutMoves.add(col);
        pOutMoves.add(row);
        return (p > 0);
    }
    
    /// Represents a possible  move
    static class ChessMove
    {
        int xSource;
        int ySource;
        int xDest;
        int yDest;
    }
    
    /// Iterates through all the possible moves for the specified color.
    static class ChessMoveIterator
    {
        int x, y;
        ArrayList<Integer> moves;
        ChessState state;
        boolean white;
        
        /// Constructs a move iterator
        ChessMoveIterator(ChessState curState, boolean whiteMoves)
        {
            x = -1;
            y = 0;
            moves = null;
            state = curState;
            white = whiteMoves;
            advance();
        }
        
        private void advance()
        {
            if(moves != null && moves.size() >= 2)
            {
                moves.remove(moves.size() - 1);
                moves.remove(moves.size() - 1);
            }
            while(y < 8 && (moves == null || moves.size() < 2))
            {
                if(++x >= 8)
                {
                    x = 0;
                    y++;
                }
                if(y < 8)
                {
                    if(state.getPiece(x, y) != ChessState.None && state.isWhite(x, y) == white)
                        moves = state.moves(x, y);
                    else
                        moves = null;
                }
            }
        }
        
        /// Returns true iff there is another move to visit
        boolean hasNext()
        {
            return (moves != null && moves.size() >= 2);
        }
        
        /// Returns the next move
        ChessState.ChessMove next()
        {
            ChessState.ChessMove m = new ChessState.ChessMove();
            m.xSource = x;
            m.ySource = y;
            m.xDest = moves.get(moves.size() - 2);
            m.yDest = moves.get(moves.size() - 1);
            advance();
            return m;
        }
    }
    
    //alpha beta pruning
    public static int alphaBeta(ChessState currentState, int depthRemaining, int alpha, int beta, boolean whitePlayer, boolean gameOver) throws Exception
    {
        ChessState.ChessMove bestMove = null;
        
        //check to see if game is over or the desired depth has been reached
        if(depthRemaining == 0 || gameOver)
        {
            return currentState.heuristic(new Random());
        }
        
        if(whitePlayer) //max
        {
            //white player's turn
            int bestValue = -100000;
            
            //setup iterator to check all valid moves
            ChessMoveIterator it = currentState.iterator(true);
            ChessState.ChessMove m;
            while(it.hasNext())
            {
                m = it.next();
                ChessState temp = new ChessState(currentState);
                boolean gameOverCheck = temp.move(m.xSource, m.ySource, m.xDest, m.yDest);
                bestValue = Math.max(bestValue, alphaBeta(temp, depthRemaining - 1, alpha, beta, false, gameOverCheck));
                
                if(bestValue > alpha)
                {
                    alpha = bestValue;
                    bestMove = m;
                }
                
                if(beta <= alpha)
                {
                    break;
                }
            }
            
            moveToMake = bestMove;
            return bestValue;
        }
        else //min
        {
            //black player's turn
            int bestValue = 100000;
            
            //setup iterator to check all valid moves
            ChessMoveIterator it = currentState.iterator(false);
            ChessState.ChessMove m;
            while(it.hasNext())
            {
                m = it.next();
                ChessState temp = new ChessState(currentState);
                boolean gameOverCheck = temp.move(m.xSource, m.ySource, m.xDest, m.yDest);
                bestValue = Math.min(bestValue, alphaBeta(temp, depthRemaining - 1, alpha, beta, true, gameOverCheck));
                
                if(bestValue < beta)
                {
                    beta = bestValue;
                    bestMove = m;
                }
                
                if(beta <= alpha)
                {
                    break;
                }
            }
            
            moveToMake = bestMove;
            return bestValue;
        }
    }
    
    public static int columnLetterToNumber(char c)
    {
        if(c == 'a' || c == 'A')
        {
            return 0;
        }
        
        if(c == 'b' || c == 'B')
        {
            return 1;
        }
        
        if(c == 'c' || c == 'C')
        {
            return 2;
        }
        
        if(c == 'd' || c == 'D')
        {
            return 3;
        }
        
        if(c == 'e' || c == 'E')
        {
            return 4;
        }
        
        if(c == 'f' || c == 'F')
        {
            return 5;
        }
        
        if(c == 'g' || c == 'G')
        {
            return 6;
        }
        
        if(c == 'h' || c == 'H')
        {
            return 7;
        }
        
        //invalid column letter
        return -1;
    }
    
    /*
     * Takes in the current state of the chessboard
     * and a boolean that indicates the color of the piece.
     * Let's a user move a chess piece and verifies that the move is valid.
     */
    public static int[] userMove(ChessState currentState, boolean whitePiece)
    {
        boolean validInput = false;
        int srcCol = -1;
        int srcRow = -1;
        int destCol = -1;
        int destRow = -1;
        Scanner terminalInput = new Scanner(System.in);
        
        while(!validInput)
        {
            String userMove = terminalInput.nextLine();
            
            if("q".equals(userMove))
            {
                //quit (returning an array with a -1 to specify that)
                int[] quit = {-1};
                return quit;
            }
            
            if(userMove.length() != 4)
            {
                System.out.println("invalid input length");
                continue;
            }
            
            srcCol = columnLetterToNumber(userMove.charAt(0));
            if(srcCol == -1)
            {
                System.out.println("invalid input for source column");
                continue;
            }
            
            try
            {
                srcRow = Integer.parseInt(userMove.substring(1, 2)) - 1;
            }
            catch(Exception e)
            {
                System.out.println("invalid input for source row");
                continue;
            }
            
            destCol = columnLetterToNumber(userMove.charAt(2));
            if(destCol == -1)
            {
                System.out.println("invalid input for destination column");
                continue;
            }
            
            try
            {
                destRow = Integer.parseInt(userMove.substring(3, 4)) - 1;
            }
            catch(Exception e)
            {
                System.out.println("invalid input for destination row");
                continue;
            }
            
            if(srcCol == -1 || srcRow == -1 || destCol == -1 || destRow == -1)
            {
                System.out.println("invalid input");
                continue;
            }
            
            if(whitePiece)
            {
                if(!currentState.isValidMove(srcCol, srcRow, destCol, destRow) || !currentState.isWhite(srcCol, srcRow))
                {
                    System.out.println("invalid move");
                }
                else
                {
                    validInput = true;
                }
            }
            else
            {
                if(!currentState.isValidMove(srcCol, srcRow, destCol, destRow) || currentState.isWhite(srcCol, srcRow))
                {
                    System.out.println("invalid move");
                }
                else
                {
                    validInput = true;
                }
            }
        }
        
        int[] moves = { srcCol, srcRow, destCol, destRow };
        return moves;
    }
    
    public static void main(String[] args) throws Exception
    {
        ChessState s = new ChessState();
        s.resetBoard();
        boolean whiteWon = false;
        boolean blackWon = false;
        int whiteDepthCheck = 0;
        int blackDepthCheck = 0;
        Scanner terminalInput = null;
        
        //verify that command line arguments are correct
        if(args.length == 2)
        {
            try
            {
                whiteDepthCheck = Integer.parseInt(args[0]);
                blackDepthCheck = Integer.parseInt(args[1]);
            }
            catch(Exception e)
            {
                System.out.println("invalid command line arguements");
                return;
            }
        }
        else
        {
            System.out.println("invalid amount of command line arguements");
            return;
        }
        
        System.out.println("White depth check is: " + whiteDepthCheck);
        System.out.println("Black depth check is: " + blackDepthCheck);
        
        //if either player is human, initalize scanner
        if(whiteDepthCheck == 0 || blackDepthCheck == 0)
        {
            terminalInput = new Scanner(System.in);
            System.out.println("Move format should be 4 characters long and "
                               + "be something like \"b1c3\" which would move "
                               + "the piece at grid position B1 to grid position C3.");
        }
        
        System.out.println("Start game");
        s.printBoard(System.out);
        
        //allow white and black to make moves until someone has won
        while(!whiteWon && !blackWon)
        {
            if(whiteDepthCheck == 0)
            {
                //human player
                System.out.println("White enter your move: ");
                
                int[] moves = userMove(s, true);
                
                if(moves[0] == -1)
                {
                    //user wants to quit the game so return
                    return;
                }
                
                whiteWon = s.move(moves[0], moves[1], moves[2], moves[3]);
            }
            else
            {
                //computer player
                //white's turn - check all moves
                alphaBeta(s, whiteDepthCheck, -100000, 100000, true, false);
                
                //white makes best move it calculated
                whiteWon = s.move(moveToMake.xSource, moveToMake.ySource, moveToMake.xDest, moveToMake.yDest);
            }
            
            //show white's move
            s.printBoard(System.out);
            System.out.println('\n');
            
            if(whiteWon)
            {
                //end game if white won
                System.out.println("White won!");
                break;
            }
            
            moveToMake = null;
            
            if(blackDepthCheck == 0)
            {
                //human player
                System.out.println("Black enter your move: ");
                
                int[] moves = userMove(s, false);
                
                if(moves[0] == -1)
                {
                    //user wants to quit the game so return
                    return;
                }
                
                blackWon = s.move(moves[0], moves[1], moves[2], moves[3]);
            }
            else
            {
                //black's turn - check all moves
                alphaBeta(s, blackDepthCheck, -100000, 100000, false, false);
                
                //white makes best move it calculated
                blackWon = s.move(moveToMake.xSource, moveToMake.ySource, moveToMake.xDest, moveToMake.yDest);
            }
            
            //show black's move
            s.printBoard(System.out);
            System.out.println('\n');
            
            if(blackWon)
            {
                //end game if black won
                System.out.println("Black won!");
                break;
            }
        }
        
        System.out.println("Game over!");
    }
}


