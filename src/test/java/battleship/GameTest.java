package battleship;

import java.util.List;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Scanner;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * Test class for Game.
 * Author: britoeabreu
 * Date: 2024-03-19
 * Time: 15:30
 * Cyclomatic Complexity for each method:
 * - Game (constructor): 1
 * - fire: 7
 * - getShots: 1
 * - getRepeatedShots: 1
 * - getInvalidShots: 1
 * - getHits: 1
 * - getSunkShips: 1
 * - getRemainingShips: 1
 * - validShot: 3
 * - repeatedShot: 2
 * - printBoard: 1
 * - printValidShots: 1
 * - printFleet: 1
 */
public class GameTest {

	private Game game;

	@BeforeEach
	void setUp() {
		game = new Game(new Fleet()); // Assuming Fleet is a concrete implementation of IFleet
	}

	@AfterEach
	void tearDown() {
		game = null;
	}

	@Test
	@DisplayName("Inicializa o jogo com listas vazias e contadores a zero")
	void constructor() {
		assertNotNull(game, "Game instance should not be null after construction.");
		assertNotNull(game.getAlienMoves(), "Shots list should not be null after initialization.");
		assertTrue(game.getAlienMoves().isEmpty(), "Shots list should be empty upon initialization.");
		assertEquals(0, game.getInvalidShots(), "Invalid shots count should be zero upon initialization.");
		assertEquals(0, game.getRepeatedShots(), "Repeated shots count should be zero upon initialization.");
		assertEquals(0, game.getHits(), "Hits count should be zero upon initialization.");
		assertEquals(0, game.getSunkShips(), "Sunk ships count should be zero upon initialization.");
	}

	@Test
	@DisplayName("Conta um tiro fora do tabuleiro como inválido")
	void fire2() {
		Position invalidPosition = new Position(-1, 5);
		game.fireSingleShot(invalidPosition, false);
		assertEquals(1, game.getInvalidShots(), "Invalid shots counter should increase for an invalid shot.");
	}

	@Test
	@DisplayName("Conta um tiro marcado como repetido")
	void fire3() {
		Position position = new Position(2, 3);
		game.fireSingleShot(position, false);
		game.fireSingleShot(position, true);
		assertEquals(1, game.getRepeatedShots(), "Repeated shots counter should increase for a repeated shot.");
	}

	@Test
	@DisplayName("Reconhece uma posição já atingida numa rajada")
	void repeatedShot1() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		Position position = new Position(2, 3);
		assertTrue(game.repeatedShot(position), "Position (2,3) should be marked as repeated after firing.");
	}

	@Test
	@DisplayName("Não considera repetida uma posição ainda não atingida")
	void repeatedShot2() {
		Position position = new Position(2, 3);
		assertFalse(game.repeatedShot(position), "Position (2,3) should not be marked as repeated before firing.");
	}

	@Test
	@DisplayName("Regista uma jogada após uma rajada")
	void getAlienMoves() {
		List<IPosition> positions = List.of(new Position(2, 3), new Position(2, 4), new Position(2, 5));
		game.fireShots(positions);
		assertEquals(1, game.getAlienMoves().size(), "Shots list should contain one shot after firing once.");
	}

	@Test
	@DisplayName("Atualiza os navios restantes após um afundamento")
	void getRemainingShips() {
		IFleet fleet = game.getMyFleet();
		Ship ship1 = new Barge(Compass.NORTH, new Position(1, 1));
		Ship ship2 = new Frigate(Compass.EAST, new Position(5, 5));

		fleet.addShip(ship1);
		assertEquals(1, game.getRemainingShips(), "Just one ship was created!");
		fleet.addShip(ship2);
		assertEquals(2, game.getRemainingShips(), "Two ships were created!");
		ship2.sink();
		assertEquals(1, game.getRemainingShips(), "Remaining ships count should be 1 after sinking one of two ships.");
	}
	@Test
	@DisplayName("Rejeita uma rajada com menos de três tiros")
	void rejeitaRajadaComPoucosTiros() {
		List<IPosition> tiros = List.of(
				new Position(0, 0),
				new Position(0, 1)
		);

		assertThrows(IllegalArgumentException.class,
				() -> game.fireShots(tiros));

		assertTrue(game.getAlienMoves().isEmpty());
		assertEquals(0, game.getHits());
		assertEquals(0, game.getInvalidShots());
		assertEquals(0, game.getRepeatedShots());
	}

	@Test
	@DisplayName("Rejeita uma rajada com mais de três tiros")
	void rejeitaRajadaComDemasiadosTiros() {
		List<IPosition> tiros = List.of(
				new Position(0, 0),
				new Position(0, 1),
				new Position(0, 2),
				new Position(0, 3)
		);

		assertThrows(IllegalArgumentException.class,
				() -> game.fireShots(tiros));

		assertTrue(game.getAlienMoves().isEmpty());
	}
	@Test
	@DisplayName("Um acerto aumenta o contador sem afundar o navio")
	void acertoSemAfundarNavio() {
		Ship navio = new Frigate(Compass.EAST, new Position(5, 5));
		game.getMyFleet().addShip(navio);
		IPosition alvo = navio.getPositions().iterator().next();

		game.fireSingleShot(alvo, false);

		assertEquals(1, game.getHits());
		assertEquals(0, game.getSunkShips());
		assertEquals(1, game.getRemainingShips());
		assertTrue(navio.stillFloating());
	}

	@Test
	@DisplayName("Atingir todas as posições afunda o navio")
	void afundaNavioAoAtingirTodasAsPosicoes() {
		Ship navio = new Frigate(Compass.EAST, new Position(5, 5));
		game.getMyFleet().addShip(navio);

		for (IPosition posicao : navio.getPositions()) {
			game.fireSingleShot(posicao, false);
		}

		assertEquals(navio.getPositions().size(), game.getHits());
		assertEquals(1, game.getSunkShips());
		assertEquals(0, game.getRemainingShips());
		assertFalse(navio.stillFloating());
	}
	@Test
	@DisplayName("Conta tiros repetidos dentro da mesma rajada")
	void tirosRepetidosNaMesmaRajada() {
		game.fireShots(List.of(
				new Position(0, 0),
				new Position(0, 0),
				new Position(0, 1)
		));

		assertEquals(1, game.getRepeatedShots());
		assertEquals(0, game.getInvalidShots());
		assertEquals(1, game.getAlienMoves().size());
	}

	@Test
	@DisplayName("Reconhece um tiro repetido numa rajada seguinte")
	void tiroRepetidoEntreRajadas() {
		game.fireShots(List.of(
				new Position(0, 0),
				new Position(0, 1),
				new Position(0, 2)
		));

		game.fireShots(List.of(
				new Position(0, 0),
				new Position(1, 0),
				new Position(1, 1)
		));

		assertEquals(1, game.getRepeatedShots());
		assertEquals(2, game.getAlienMoves().size());
		assertTrue(game.repeatedShot(new Position(1, 1)));
		assertFalse(game.repeatedShot(new Position(9, 9)));
	}
	@Test
	@DisplayName("Lê três posições com letra e número separados")
	void lePosicoesSeparadas() {
		try (Scanner entrada = new Scanner("A 1 B 2 C 3")) {
			String json = game.readEnemyFire(entrada);

			assertNotNull(json);
			assertEquals(1, game.getAlienMoves().size());

			List<IPosition> tiros = game.getAlienMoves().get(0).getShots();
			assertEquals(3, tiros.size());
			assertEquals(new Position('A', 1), tiros.get(0));
			assertEquals(new Position('B', 2), tiros.get(1));
			assertEquals(new Position('C', 3), tiros.get(2));
		}
	}

	@Test
	@DisplayName("Rejeita uma posição sem o número")
	void rejeitaPosicaoIncompleta() {
		try (Scanner entrada = new Scanner("A")) {
			assertThrows(IllegalArgumentException.class,
					() -> game.readEnemyFire(entrada));

			assertTrue(game.getAlienMoves().isEmpty());
		}
	}

	@Test
	@DisplayName("Rejeita uma entrada com apenas duas posições")
	void rejeitaEntradaComDuasPosicoes() {
		try (Scanner entrada = new Scanner("A 1 B 2")) {
			assertThrows(IllegalArgumentException.class,
					() -> game.readEnemyFire(entrada));

			assertTrue(game.getAlienMoves().isEmpty());
		}
	}
	@Test
	@DisplayName("Inicializa as frotas e a lista de jogadas próprias")
	void inicializaFrotasEJogadas() {
		assertNotNull(game.getMyFleet());
		assertNotNull(game.getAlienFleet());
		assertNotNull(game.getMyMoves());
		assertTrue(game.getMyMoves().isEmpty());
	}

	@Test
	@DisplayName("Gera uma rajada aleatória com três posições válidas e distintas")
	void geraRajadaAleatoria() {
		String json = game.randomEnemyFire();

		assertNotNull(json);
		assertEquals(1, game.getAlienMoves().size());

		List<IPosition> tiros = game.getAlienMoves().get(0).getShots();

		assertEquals(3, tiros.size());
		assertEquals(3L, tiros.stream().distinct().count());
		assertEquals(0, game.getInvalidShots());
		assertEquals(0, game.getRepeatedShots());

		for (IPosition tiro : tiros) {
			assertNotNull(tiro);
			assertTrue(game.repeatedShot(tiro));
		}
	}
	@Test
	@DisplayName("Imprime o tabuleiro vazio com legenda")
	void imprimeTabuleiroVazioComLegenda() {
		ByteArrayOutputStream captura = new ByteArrayOutputStream();
		PrintStream original = System.out;

		try (PrintStream saida = new PrintStream(captura)) {
			System.setOut(saida);
			game.printMyBoard(false, true);
		} finally {
			System.setOut(original);
		}

		String texto = captura.toString();

		assertTrue(texto.contains("LEGENDA"));
		assertTrue(texto.contains(" A | . . . . . . . . . . |"));
		assertTrue(texto.contains(" J | . . . . . . . . . . |"));
	}

	@Test
	@DisplayName("Mostra os tiros na água e permite ocultá-los")
	void mostraEOcultaTirosNaAgua() {
		game.fireShots(List.of(
				new Position(0, 0),
				new Position(0, 1),
				new Position(0, 2)
		));

		ByteArrayOutputStream captura = new ByteArrayOutputStream();
		PrintStream original = System.out;

		try (PrintStream saida = new PrintStream(captura)) {
			System.setOut(saida);

			game.printMyBoard(true, false);
			String comTiros = captura.toString();
			assertTrue(comTiros.contains(" A | o o o . . . . . . . |"));
			assertFalse(comTiros.contains("LEGENDA"));

			captura.reset();

			game.printMyBoard(false, false);
			String semTiros = captura.toString();
			assertTrue(semTiros.contains(" A | . . . . . . . . . . |"));
		} finally {
			System.setOut(original);
		}
	}
	@Test
	@DisplayName("Imprime o tabuleiro adversário vazio sem legenda")
	void imprimeTabuleiroAdversario() {
		ByteArrayOutputStream captura = new ByteArrayOutputStream();
		PrintStream original = System.out;

		try (PrintStream saida = new PrintStream(captura)) {
			System.setOut(saida);
			game.printAlienBoard(true, false);
		} finally {
			System.setOut(original);
		}

		String texto = captura.toString();

		assertTrue(texto.contains(" A | . . . . . . . . . . |"));
		assertTrue(texto.contains(" J | . . . . . . . . . . |"));
		assertFalse(texto.contains("LEGENDA"));
	}

	@Test
	@DisplayName("Representa o navio e assinala um tiro certeiro")
	void representaNavioEAcerto() {
		Ship navio = new Frigate(Compass.EAST, new Position(5, 5));
		game.getMyFleet().addShip(navio);

		IPosition alvo = navio.getPositions().iterator().next();

		ByteArrayOutputStream captura = new ByteArrayOutputStream();
		PrintStream original = System.out;

		try (PrintStream saida = new PrintStream(captura)) {
			System.setOut(saida);

			game.printMyBoard(false, false);
			assertTrue(captura.toString().contains("#"));
			assertFalse(captura.toString().contains("*"));

			game.fireShots(List.of(
					alvo,
					new Position(0, 0),
					new Position(0, 1)
			));

			captura.reset();
			game.printMyBoard(true, false);

			String texto = captura.toString();
			assertTrue(texto.contains("*"));
			assertTrue(texto.contains("#"));
			assertTrue(texto.contains(" A | o o . . . . . . . . |"));
		} finally {
			System.setOut(original);
		}
	}
	@Test
	@DisplayName("Assinala as posições adjacentes a um navio afundado")
	void assinalaAdjacentesDoNavioAfundado() {
		Ship navio = new Frigate(Compass.EAST, new Position(5, 5));
		game.getMyFleet().addShip(navio);
		navio.sink();

		ByteArrayOutputStream captura = new ByteArrayOutputStream();
		PrintStream original = System.out;

		try (PrintStream saida = new PrintStream(captura)) {
			System.setOut(saida);
			game.printMyBoard(false, false);
		} finally {
			System.setOut(original);
		}

		String texto = captura.toString();

		assertFalse(navio.stillFloating());
		assertTrue(texto.contains("#"));
		assertTrue(texto.contains(" -"));
	}

	@Test
	@DisplayName("Completa a rajada quando resta apenas uma posição disponível")
	void rajadaAleatoriaComUmaPosicaoDisponivel() {
		// Dispara sobre as primeiras 99 posições do tabuleiro.
		for (int i = 0; i < 99; i += 3) {
			game.fireShots(List.of(
					new Position(i / 10, i % 10),
					new Position((i + 1) / 10, (i + 1) % 10),
					new Position((i + 2) / 10, (i + 2) % 10)
			));
		}

		String json = game.randomEnemyFire();

		assertNotNull(json);
		assertEquals(34, game.getAlienMoves().size());

		List<IPosition> tiros = game.getAlienMoves().get(33).getShots();

		assertEquals(3, tiros.size());
		for (IPosition tiro : tiros) {
			assertEquals(new Position(9, 9), tiro);
		}

		assertEquals(2, game.getRepeatedShots());
		assertEquals(0, game.getInvalidShots());
	}
	@Test
	@DisplayName("Apresenta a mensagem de fim do jogo")
	void apresentaMensagemDeFim() {
		ByteArrayOutputStream captura = new ByteArrayOutputStream();
		PrintStream original = System.out;

		try (PrintStream saida = new PrintStream(captura)) {
			System.setOut(saida);
			game.over();
		} finally {
			System.setOut(original);
		}

		String texto = captura.toString();

		assertTrue(texto.contains("Java Sparrow"));
		assertTrue(texto.contains("glub glub glub"));
	}

	@Test
	@DisplayName("Lê três posições com letra e número juntos")
	void lePosicoesJuntas() {
		try (Scanner entrada = new Scanner("A1 B2 C3")) {
			String json = game.readEnemyFire(entrada);

			assertNotNull(json);
			assertEquals(1, game.getAlienMoves().size());

			List<IPosition> tiros = game.getAlienMoves().get(0).getShots();

			assertEquals(3, tiros.size());
			assertEquals(new Position('A', 1), tiros.get(0));
			assertEquals(new Position('B', 2), tiros.get(1));
			assertEquals(new Position('C', 3), tiros.get(2));
		}
	}
}
