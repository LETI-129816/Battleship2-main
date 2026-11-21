package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * TasksTest
 *
 * @author Your Name
 * Date: 07/01/2026
 * Time: 00:53
 */

class TasksTest {

	private static final String FICHEIRO = "tasks_teste_jogo";

	private final InputStream originalIn = System.in;
	private final PrintStream originalOut = System.out;
	private ByteArrayOutputStream out;
	private Tasks tasks;

	@BeforeEach
	void setUp() {
		tasks = new Tasks();
		out = new ByteArrayOutputStream();
		System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
	}

	@AfterEach
	void tearDown() throws Exception {
		System.setIn(originalIn);
		System.setOut(originalOut);
		Files.deleteIfExists(Path.of(FICHEIRO + ".json"));
		tasks = null;
		out = null;
	}

	// ---------- auxiliares ----------

	private String saida() {
		return out.toString(StandardCharsets.UTF_8);
	}

	/**
	 * Corre o menu com as linhas dadas (já inclui 'desisto' no fim) e devolve o que foi impresso.
	 */
	private String executaMenu(String... linhas) {
		out.reset();
		String input = String.join("\n", linhas) + "\ndesisto\n";
		System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
		Tasks.menu();
		return saida();
	}

	private void assertTermina(String saida) {
		assertTrue(saida.contains("Bons ventos!"),
				"Error: expected the menu to end with the goodbye message but it did not");
	}

	/**
	 * Sequência de FLEET_SIZE barcas afastadas entre si (linha/coluna pares), válida para addShip.
	 */
	private String frotaValida() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < Fleet.FLEET_SIZE; i++) {
			sb.append("barca ").append((i / 5) * 2).append(' ').append((i % 5) * 2).append(" n\n");
		}
		return sb.toString();
	}

	// ---------- construtor ----------

	@Test
	@DisplayName("construtor cria uma instância de Tasks")
	void constructor() {
		assertNotNull(tasks, "Error: expected a Tasks instance but got null");
	}

	// ---------- readPosition (CC 1) ----------

	@Test
	@DisplayName("readPosition lê linha e coluna como inteiros")
	void readPosition() {
		Position p = Tasks.readPosition(new Scanner("3 4"));
		assertEquals(new Position(3, 4), p, "Error: expected position (3,4) but got " + p);
	}

	// ---------- readClassicPosition (CC 8; os caminhos 7 e 8 são inalcançáveis) ----------

	@Test
	@DisplayName("readClassicPosition sem input lança IllegalArgumentException")
	void readClassicPosition1() {
		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(new Scanner("")),
				"Error: expected IllegalArgumentException for empty input");
	}

	@Test
	@DisplayName("readClassicPosition aceita o formato compacto 'A3'")
	void readClassicPosition2() {
		IPosition p = Tasks.readClassicPosition(new Scanner("A3"));
		assertEquals(new Position('A', 3), p, "Error: expected A3 but got " + p);
	}

	@Test
	@DisplayName("readClassicPosition aceita minúsculas e espaço: 'a 3'")
	void readClassicPosition3() {
		IPosition p = Tasks.readClassicPosition(new Scanner("a 3"));
		assertEquals(new Position('A', 3), p, "Error: expected A3 but got " + p);
	}

	@Test
	@DisplayName("readClassicPosition com um só token inválido ('33') lança exceção")
	void readClassicPosition4() {
		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(new Scanner("33")),
				"Error: expected IllegalArgumentException for '33'");
	}

	@Test
	@DisplayName("readClassicPosition com coluna inválida ('AB 3') lança exceção")
	void readClassicPosition5() {
		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(new Scanner("AB 3")),
				"Error: expected IllegalArgumentException for 'AB 3'");
	}

	@Test
	@DisplayName("readClassicPosition com linha não numérica ('A -3') lança exceção")
	void readClassicPosition6() {
		assertThrows(IllegalArgumentException.class,
				() -> Tasks.readClassicPosition(new Scanner("A -3")),
				"Error: expected IllegalArgumentException for 'A -3'");
	}
	// NOTA: o 'else if' com resultado verdadeiro é inalcançável: se part1 é uma letra e part2 são dígitos,
	// a concatenação já satisfaz a primeira regex e entra no 'if' anterior.

	// ---------- readShip (CC 1) ----------

	@Test
	@DisplayName("readShip constrói o navio descrito no input")
	void readShip() {
		Ship s = Tasks.readShip(new Scanner("barca 3 4 n"));
		assertNotNull(s, "Error: expected a ship but got null");
		assertEquals(new Position(3, 4), s.getPosition(),
				"Error: expected position (3,4) but got " + s.getPosition());
	}

	@Test
	@DisplayName("readShip com tipo de navio desconhecido devolve null")
	void readShipDesconhecido() {
		assertNull(Tasks.readShip(new Scanner("xpto 3 4 n")),
				"Error: expected null for unknown ship kind");
	}

	@Test
	@DisplayName("readShip com scanner null lança AssertionError")
	void readShipNull() {
		assertThrows(AssertionError.class, () -> Tasks.readShip(null),
				"Error: expected AssertionError for null scanner (requires -ea)");
	}

	// ---------- buildFleet (CC 4) ----------

	@Test
	@DisplayName("buildFleet constrói uma frota completa com navios válidos")
	void buildFleet1() {
		Fleet f = Tasks.buildFleet(new Scanner(frotaValida()));
		assertEquals(Fleet.FLEET_SIZE, f.getShips().size(),
				"Error: expected a full fleet but got " + f.getShips().size() + " ships");
	}

	@Test
	@DisplayName("buildFleet ignora um navio desconhecido e continua")
	void buildFleet2() {
		Fleet f = Tasks.buildFleet(new Scanner("xpto 0 0 n\n" + frotaValida()));
		assertEquals(Fleet.FLEET_SIZE, f.getShips().size(),
				"Error: expected the unknown ship to be skipped");
	}

	@Test
	@DisplayName("buildFleet rejeita um navio sobreposto e continua")
	void buildFleet3() {
		Fleet f = Tasks.buildFleet(new Scanner("barca 0 0 n\nbarca 0 0 n\n" + frotaValida()));
		assertEquals(Fleet.FLEET_SIZE, f.getShips().size(),
				"Error: expected the overlapping ship to be rejected");
	}

	@Test
	@DisplayName("buildFleet com scanner null lança AssertionError")
	void buildFleet4() {
		assertThrows(AssertionError.class, () -> Tasks.buildFleet(null),
				"Error: expected AssertionError for null scanner (requires -ea)");
	}

	// ---------- menuHelp (CC 1) ----------

	@Test
	@DisplayName("menuHelp imprime todos os comandos disponíveis")
	void menuHelp() {
		Tasks.menuHelp();
		String s = saida();
		for (String comando : new String[]{"gerafrota", "lefrota", "estado", "mapa", "rajada",
				"simula", "tiros", "guardar", "carregar", "desisto"}) {
			assertTrue(s.contains(comando), "Error: help is missing the command " + comando);
		}
	}


	// ---------- menu (CC 26) ----------

	@Test
	@DisplayName("menu termina logo com 'desisto'")
	void menu1() {
		String s = executaMenu();
		assertAll(
				() -> assertTermina(s),
				() -> assertTrue(s.contains("AJUDA DO MENU"), "Error: expected help on start")
		);
	}

	@Test
	@DisplayName("menu: gerafrota cria jogo e imprime o mapa")
	void menu2() {
		String base = executaMenu();
		String s = executaMenu("gerafrota");
		assertTrue(s.length() > base.length(),
				"Error: expected gerafrota to print the board, but output did not grow");
	}

	@Test
	@DisplayName("menu: lefrota constrói a frota a partir do input")
	void menu3() {
		String base = executaMenu();
		String s = executaMenu("lefrota", frotaValida().trim());
		assertTrue(s.length() > base.length(),
				"Error: expected lefrota to print the board, but output did not grow");
	}

	@Test
	@DisplayName("menu: estado com frota imprime o estado")
	void menu4() {
		String semEstado = executaMenu("gerafrota");
		String comEstado = executaMenu("gerafrota", "estado");
		assertTrue(comEstado.length() > semEstado.length() + 2,
				"Error: expected estado to print the fleet status");
	}

	@Test
	@DisplayName("menu: estado sem frota não rebenta")
	void menu5() {
		assertDoesNotThrow(() -> assertTermina(executaMenu("estado")),
				"Error: estado without a fleet should not throw");
	}

	@Test
	@DisplayName("menu: mapa com frota volta a imprimir o mapa")
	void menu6() {
		String semMapa = executaMenu("gerafrota");
		String comMapa = executaMenu("gerafrota", "mapa");
		assertTrue(comMapa.length() > semMapa.length() + 2,
				"Error: expected mapa to print the board again");
	}

	@Test
	@DisplayName("menu: mapa sem frota não rebenta")
	void menu7() {
		assertDoesNotThrow(() -> assertTermina(executaMenu("mapa")),
				"Error: mapa without a fleet should not throw");
	}

	@Test
	@DisplayName("menu: rajada com jogo dispara três tiros e continua")
	void menu8() {
		assertTermina(executaMenu("gerafrota", "rajada A1 B1 C1"));
	}

	@Test
	@DisplayName("menu: rajada sem jogo não rebenta")
	void menu9() {
		assertDoesNotThrow(() -> assertTermina(executaMenu("rajada")),
				"Error: rajada without a game should not throw");
	}


	@Test
	@DisplayName("menu: simula sem jogo não rebenta")
	void menu11() {
		assertDoesNotThrow(() -> assertTermina(executaMenu("simula")),
				"Error: simula without a game should not throw");
	}

	@Test
	@DisplayName("menu: tiros com jogo imprime os tiros")
	void menu13() {
		String semTiros = executaMenu("gerafrota");
		String comTiros = executaMenu("gerafrota", "tiros");
		assertTrue(comTiros.length() > semTiros.length() + 2,
				"Error: expected tiros to print the shots board");
	}

	@Test
	@DisplayName("menu: tiros sem jogo não rebenta")
	void menu14() {
		assertDoesNotThrow(() -> assertTermina(executaMenu("tiros")),
				"Error: tiros without a game should not throw");
	}

	@Test
	@DisplayName("menu: guardar com jogo cria o ficheiro JSON")
	void menu15() {
		String s = executaMenu("gerafrota", "guardar", FICHEIRO);
		assertAll(
				() -> assertTrue(s.contains("guardado com sucesso"), "Error: expected a success message"),
				() -> assertTrue(new File(FICHEIRO + ".json").exists(), "Error: expected the JSON file to exist")
		);
	}

	@Test
	@DisplayName("menu: guardar sem jogo avisa que não há jogo")
	void menu16() {
		String s = executaMenu("guardar");
		assertTrue(s.contains("nenhum jogo"), "Error: expected a 'no game' message but got: " + s);
	}

	@Test
	@DisplayName("menu: guardar numa pasta inexistente mostra erro")
	void menu17() {
		String s = executaMenu("gerafrota", "guardar", "pasta_inexistente/jogo");
		assertTrue(s.contains("Erro ao guardar"), "Error: expected a save error message but got: " + s);
	}

	@Test
	@DisplayName("menu: carregar um jogo guardado funciona")
	void menu18() {
		String s = executaMenu("gerafrota", "guardar", FICHEIRO, "carregar", FICHEIRO);
		assertTrue(s.contains("carregado com sucesso"), "Error: expected a load success message");
	}

	@Test
	@DisplayName("menu: carregar um ficheiro inexistente mostra erro")
	void menu19() {
		String s = executaMenu("carregar", "ficheiro_que_nao_existe");
		assertTrue(s.contains("encontrado"), "Error: expected a 'file not found' message but got: " + s);
	}

	@Test
	@DisplayName("menu: carregar um ficheiro corrompido mostra erro")
	void menu20() throws Exception {
		Files.writeString(Path.of(FICHEIRO + ".json"), "isto nao e json {");
		String s = executaMenu("carregar", FICHEIRO);
		assertTrue(s.contains("reconstruir"), "Error: expected a rebuild error message but got: " + s);
	}

	@Test
	@DisplayName("menu: carregar um ficheiro vazio mostra erro")
	void menu21() throws Exception {
		Files.writeString(Path.of(FICHEIRO + ".json"), "");
		String s = executaMenu("carregar", FICHEIRO);
		assertTrue(s.contains("reconstruir"), "Error: expected a rebuild error message but got: " + s);
	}

	@Test
	@DisplayName("menu: ajuda volta a mostrar os comandos")
	void menu22() {
		String base = executaMenu();
		String s = executaMenu("ajuda");
		assertTrue(s.length() > base.length() + 100,
				"Error: expected ajuda to print the help a second time");
	}

	@Test
	@DisplayName("menu: comando desconhecido mostra mensagem e continua")
	void menu23() {
		String s = executaMenu("xpto");
		assertAll(
				() -> assertTrue(s.contains("Que comando"), "Error: expected the unknown-command message"),
				() -> assertTermina(s)
		);
	}

	/**
	 * Rajadas sobre 99 células do tabuleiro 10x10 (33 rajadas de 3 tiros). Assume linhas 1-10 e colunas A-J.
	 */
	private String rajadasSobreOTabuleiro() {
		StringBuilder sb = new StringBuilder();
		int contador = 0;
		for (int linha = 1; linha <= 10; linha++) {
			for (char coluna = 'A'; coluna <= 'J'; coluna++) {
				if (linha == 10 && coluna == 'J') break;
				if (contador % 3 == 0) sb.append("rajada");
				sb.append(' ').append(coluna).append(linha);
				if (contador % 3 == 2) sb.append('\n');
				contador++;
			}
		}
		return sb.toString();
	}

	@Test
	@Disabled("Chama System.exit(0) quando a frota é afundada; não é testável sem refabricar (Java 24+ não permite intercetar System.exit)")
	@DisplayName("menu: rajada que afunda a frota termina o programa")
	void menu10() {
	}

	@Test
	@Disabled("Ciclo com Thread.sleep(3000) por tiro e System.exit(0) no fim")
	@DisplayName("menu: simula com jogo corre até ao fim")
	void menu12() {
	}

	@Test
	@DisplayName("menu: carregar um ficheiro com tipo de navio inexistente mostra erro")
	void menu24() throws Exception {
		executaMenu("gerafrota", "guardar", FICHEIRO);

		Path ficheiro = Path.of(FICHEIRO + ".json");
		String json = Files.readString(ficheiro);
		assertTrue(json.contains("tipo_navio_exato"),
				"Error: expected the saved file to contain the ship type tag");

		// Troca o nome da classe de cada navio por uma classe que não existe
		String estragado = json.replaceAll(
				"(\"tipo_navio_exato\"\\s*:\\s*\")[^\"]+\"", "$1battleship.Xpto\"");
		Files.writeString(ficheiro, estragado);

		String s = executaMenu("carregar", FICHEIRO);
		assertTrue(s.contains("reconstruir"),
				"Error: expected a rebuild error message but got: " + s);
	}

}