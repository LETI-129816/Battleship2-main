package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class IMoveTest {

    @Test
    @DisplayName("readMove lê o número de tiros e as posições")
    void testReadMove() {
        Scanner sc = new Scanner("2 1 2 3 4");

        Move move = IMove.readMove(5, sc);

        assertEquals(5, move.getNumber());
        assertEquals(2, move.getShots().size());
        assertTrue(move.getShotResults().isEmpty());
    }

    @Test
    @DisplayName("readMove com zero tiros devolve uma jogada sem tiros")
    void testReadMoveSemTiros() {
        Scanner sc = new Scanner("0");

        Move move = IMove.readMove(1, sc);

        assertEquals(1, move.getNumber());
        assertTrue(move.getShots().isEmpty());
        assertTrue(move.getShotResults().isEmpty());
    }

    @Test
    @DisplayName("readMove devolve uma Move que implementa IMove")
    void testReadMoveTipo() {
        IMove move = IMove.readMove(3, new Scanner("1 1 1"));

        assertEquals(3, move.getNumber());
        assertEquals(1, move.getShots().size());
    }
}