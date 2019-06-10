package gameModules;

import boards.EnemyBoard;
import boards.GameStatus;
import boards.PlayerBoard;
import boards.fields.FieldStatus;
import connection.Connection;
import coordinates.Coordinate;
import coordinates.CoordinateImpl;
import exceptions.FieldException;
import exceptions.InputException;
import exceptions.StatusException;
import output.OutputSymbols;

import java.awt.*;
import java.io.*;

/**
 * @author s0568823 - Leon Enzenberger
 */
public class communicationInstance extends Thread {
    private static OutputStream OUTPUT;
    private static InputStream INPUT;
    private static int REFRESH_RATE;
    private static boolean CONNECTION_IN_USE;

    public communicationInstance(int port) throws IOException {
        Connection connection = new Connection(port);
        connection.start();
        OUTPUT = connection.getOutputStream();
        INPUT = connection.getInputStream();
        CONNECTION_IN_USE =false;
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = env.getScreenDevices();
        for (GraphicsDevice device : devices) {
            int displayRefreshRate = device.getDisplayMode().getRefreshRate();
            if (displayRefreshRate > REFRESH_RATE) REFRESH_RATE = displayRefreshRate;
        }
    }

    @Override
    public synchronized void run() {
        InputStreamReader inputStreamReader = new InputStreamReader(INPUT);
        BufferedReader inputBuffer = new BufferedReader(inputStreamReader);
        //noinspection InfiniteLoopStatement
        while (true) {
            while(!CONNECTION_IN_USE) {
                CONNECTION_IN_USE = true;
                try {
                    try {
                        String line = inputBuffer.readLine();
                        enemyInput(line);
                    } catch (StatusException | InputException | FieldException e) {
                        OUTPUT.write("NetworkException".getBytes());
                    }
                } catch (IOException ignored) { }
                CONNECTION_IN_USE=false;
            }
            try {
                Thread.sleep((1000 / REFRESH_RATE)+1);
            } catch (InterruptedException ignored) { }

        }
    }

    private void enemyInput(String commandString) throws StatusException, InputException, FieldException {
        PlayerBoard playerBoard = gameInstance.getPlayerBoard();
        EnemyBoard enemyBoard = gameInstance.getEnemyBoard();

        String[] commandArray = commandString.trim().toUpperCase().split(" ");
        String command = commandArray[0];

        if (command.equals("READY")) {
            if (enemyBoard.getStatus() != GameStatus.PREPARATION) throw new StatusException();
            enemyBoard.setStatus(GameStatus.READY);
        } else if (command.equals("REVOKE")) {
            if (enemyBoard.getStatus() != GameStatus.READY) throw new StatusException();
            enemyBoard.setStatus(GameStatus.PREPARATION);
        } else if (command.matches("\\D")) {
            if (playerBoard.getStatus() != GameStatus.RECEIVE) throw new StatusException();
            if (commandArray.length != 2) throw new InputException();
            int xCoordinate = OutputSymbols.getNumber(commandArray[0].charAt(0));
            int yCoordinate = Integer.parseInt(commandArray[1]);
            CoordinateImpl attackedField = new CoordinateImpl(xCoordinate, yCoordinate);
            FieldStatus shotResult = playerBoard.receiveAttack(attackedField);
        }
    }

    public synchronized FieldStatus attackEnemy(Coordinate coordinate)throws InputException, IOException{
        boolean attackSend=false;
        FieldStatus attackedFieldStatus=null;
        while(!attackSend){
            if(!CONNECTION_IN_USE){
                CONNECTION_IN_USE=true;
                int xCoordinate=coordinate.getYCoordinate();
                char yCoordinate=OutputSymbols.getAlphabet(coordinate.getYCoordinate());
                OUTPUT.write((yCoordinate + " " + xCoordinate).getBytes());
                attackSend=true;

                CONNECTION_IN_USE=false;
            }
            else{
                try{
                    Thread.sleep(1);
                } catch (InterruptedException ignored){}
            }
        }
    }
}
