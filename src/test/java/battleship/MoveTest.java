package battleship;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private PrintStream originalOut;
    private ByteArrayOutputStream capturedOut;

    @BeforeEach
    void setUp() {
        originalOut = System.out;
        capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    // ---------- auxiliares ----------

    /** IShip falso: só responde a getCategory(), que é o que a Move usa. */
    private static IShip ship(String category) {
        return (IShip) Proxy.newProxyInstance(
                IShip.class.getClassLoader(),
                new Class<?>[]{IShip.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getCategory" -> category;
                    case "equals" -> proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "ShipFalso(" + category + ")";
                    default -> null;
                });
    }

    private static IGame.ShotResult invalid() {
        return new IGame.ShotResult(false, false, null, false);
    }

    private static IGame.ShotResult repeated() {
        return new IGame.ShotResult(true, true, null, false);
    }

    private static IGame.ShotResult miss() {
        return new IGame.ShotResult(true, false, null, false);
    }

    private static IGame.ShotResult hit(IShip s) {
        return new IGame.ShotResult(true, false, s, false);
    }

    private static IGame.ShotResult sunk(IShip s) {
        return new IGame.ShotResult(true, false, s, true);
    }

    private static Move moveWith(IGame.ShotResult... results) {
        return new Move(1, new ArrayList<>(), new ArrayList<>(List.of(results)));
    }

    private static JsonNode json(String text) throws Exception {
        return MAPPER.readTree(text);
    }

    private String consola() {
        return capturedOut.toString();
    }

    // ---------- construtor, getters e toString ----------

    @Test
    @DisplayName("Os getters devolvem os valores do construtor")
    void testGetters() {
        List<IPosition> shots = new ArrayList<>();
        shots.add(new Position(1, 2));
        List<IGame.ShotResult> results = new ArrayList<>();
        results.add(miss());

        Move move = new Move(4, shots, results);

        assertEquals(4, move.getNumber());
        assertSame(shots, move.getShots());
        assertSame(results, move.getShotResults());
    }

    @Test
    @DisplayName("toString mostra número, tiros e resultados")
    void testToString() {
        List<IPosition> shots = new ArrayList<>();
        shots.add(new Position(1, 2));

        Move move = new Move(2, shots, new ArrayList<>());

        assertEquals("Move{number=2, shots=1, results=0}", move.toString());
    }

    // ---------- JSON devolvido ----------

    @Test
    @DisplayName("Tiros inválidos são ignorados e contam como exteriores")
    void testTirosInvalidos() throws Exception {
        JsonNode r = json(moveWith(invalid(), invalid(), invalid()).processEnemyFire(false));

        assertEquals(0, r.get("validShots").asInt());
        assertEquals(0, r.get("repeatedShots").asInt());
        assertEquals(0, r.get("missedShots").asInt());
        assertEquals(3, r.get("outsideShots").asInt());
        assertEquals(0, r.get("sunkBoats").size());
        assertEquals(0, r.get("hitsOnBoats").size());
    }

    @Test
    @DisplayName("Sem resultados, todos os tiros são exteriores")
    void testSemResultados() throws Exception {
        JsonNode r = json(moveWith().processEnemyFire(false));

        assertEquals(0, r.get("validShots").asInt());
        assertEquals(3, r.get("outsideShots").asInt());
    }

    @Test
    @DisplayName("Tiros na água contam como válidos e falhados")
    void testTirosNaAgua() throws Exception {
        JsonNode r = json(moveWith(miss(), miss(), miss()).processEnemyFire(false));

        assertEquals(3, r.get("validShots").asInt());
        assertEquals(3, r.get("missedShots").asInt());
        assertEquals(0, r.get("outsideShots").asInt());
        assertEquals(0, r.get("repeatedShots").asInt());
    }

    @Test
    @DisplayName("Tiros repetidos não contam como válidos")
    void testTirosRepetidos() throws Exception {
        JsonNode r = json(moveWith(repeated(), repeated(), miss()).processEnemyFire(false));

        assertEquals(1, r.get("validShots").asInt());
        assertEquals(2, r.get("repeatedShots").asInt());
        assertEquals(1, r.get("missedShots").asInt());
        assertEquals(0, r.get("outsideShots").asInt());
    }

    @Test
    @DisplayName("Acerto sem afundar aparece em hitsOnBoats")
    void testAcertoSemAfundar() throws Exception {
        JsonNode r = json(moveWith(hit(ship("Fragata")), hit(ship("Fragata"))).processEnemyFire(false));

        assertEquals(2, r.get("validShots").asInt());
        assertEquals(0, r.get("sunkBoats").size());
        assertEquals(1, r.get("hitsOnBoats").size());
        assertEquals("Fragata", r.get("hitsOnBoats").get(0).get("type").asText());
        assertEquals(2, r.get("hitsOnBoats").get(0).get("hits").asInt());
    }

    @Test
    @DisplayName("Barco afundado aparece em sunkBoats e não em hitsOnBoats")
    void testBarcoAfundado() throws Exception {
        JsonNode r = json(moveWith(sunk(ship("Barca"))).processEnemyFire(false));

        assertEquals(1, r.get("sunkBoats").size());
        assertEquals("Barca", r.get("sunkBoats").get(0).get("type").asText());
        assertEquals(1, r.get("sunkBoats").get(0).get("count").asInt());
        assertEquals(0, r.get("hitsOnBoats").size());
    }

    @Test
    @DisplayName("Vários barcos do mesmo tipo afundados são contados")
    void testVariosAfundadosMesmoTipo() throws Exception {
        JsonNode r = json(moveWith(sunk(ship("Barca")), sunk(ship("Barca"))).processEnemyFire(false));

        assertEquals(1, r.get("sunkBoats").size());
        assertEquals(2, r.get("sunkBoats").get(0).get("count").asInt());
    }

    @Test
    @DisplayName("Mistura de afundado, acerto, água, repetido e exterior")
    void testMistura() throws Exception {
        JsonNode r = json(moveWith(sunk(ship("Barca")), hit(ship("Fragata")), repeated())
                .processEnemyFire(false));

        assertEquals(2, r.get("validShots").asInt());
        assertEquals(1, r.get("repeatedShots").asInt());
        assertEquals(0, r.get("outsideShots").asInt());
        assertEquals(1, r.get("sunkBoats").size());
        assertEquals(1, r.get("hitsOnBoats").size());
        assertEquals("Fragata", r.get("hitsOnBoats").get(0).get("type").asText());
    }

    // ---------- modo verbose = false ----------

    @Test
    @DisplayName("Sem verbose não escreve a linha resumo, mas escreve o JSON")
    void testSemVerbose() {
        moveWith(miss()).processEnemyFire(false);

        assertFalse(consola().contains("Jogada nº"));
        assertTrue(consola().contains("validShots"));
    }

    // ---------- modo verbose = true ----------

    @Test
    @DisplayName("Verbose: só tiros na água")
    void testVerboseAgua() {
        moveWith(miss(), miss(), miss()).processEnemyFire(true);

        assertTrue(consola().contains("Jogada nº1 -> 3 tiros válidos: 3 tiros na água"));
    }

    @Test
    @DisplayName("Verbose: um tiro na água (singular) e dois exteriores")
    void testVerboseUmTiroNaAgua() {
        moveWith(miss()).processEnemyFire(true);

        assertTrue(consola().contains("1 tiro válido: 1 tiro na água"));
        assertTrue(consola().contains("2 tiros exteriores"));
    }

    @Test
    @DisplayName("Verbose: um barco afundado (singular)")
    void testVerboseUmAfundado() {
        moveWith(sunk(ship("Barca"))).processEnemyFire(true);

        assertTrue(consola().contains("1 tiro válido: 1 Barca ao fundo"));
    }

    @Test
    @DisplayName("Verbose: dois barcos do mesmo tipo afundados (plural)")
    void testVerboseDoisAfundados() {
        moveWith(sunk(ship("Barca")), sunk(ship("Barca"))).processEnemyFire(true);

        assertTrue(consola().contains("2 tiros válidos: 2 Barcas ao fundo"));
        assertTrue(consola().contains("1 tiro exterior"));
    }

    @Test
    @DisplayName("Verbose: acertos sem afundar")
    void testVerboseAcertos() {
        moveWith(hit(ship("Fragata")), hit(ship("Fragata"))).processEnemyFire(true);

        assertTrue(consola().contains("2 tiros num(a) Fragata"));
    }

    @Test
    @DisplayName("Verbose: um acerto sem afundar (singular)")
    void testVerboseUmAcerto() {
        moveWith(hit(ship("Fragata"))).processEnemyFire(true);

        assertTrue(consola().contains("1 tiro num(a) Fragata"));
    }

    @Test
    @DisplayName("Verbose: acerto, água e afundado juntos")
    void testVerboseMistura() {
        moveWith(sunk(ship("Barca")), hit(ship("Fragata")), miss()).processEnemyFire(true);

        String texto = consola();
        assertTrue(texto.contains("3 tiros válidos: "));
        assertTrue(texto.contains("1 Barca ao fundo"));
        assertTrue(texto.contains("1 tiro num(a) Fragata"));
        assertTrue(texto.contains("1 tiro na água"));
    }

    @Test
    @DisplayName("Verbose: só tiros repetidos")
    void testVerboseSoRepetidos() {
        moveWith(repeated(), repeated()).processEnemyFire(true);

        assertTrue(consola().contains("Jogada nº1 -> 2 tiros repetidos"));
        assertTrue(consola().contains("1 tiro exterior"));
    }

    @Test
    @DisplayName("Verbose: um só tiro repetido (singular)")
    void testVerboseUmRepetido() {
        moveWith(repeated()).processEnemyFire(true);

        assertTrue(consola().contains("1 tiro repetido"));
        assertTrue(consola().contains("2 tiros exteriores"));
    }

    @Test
    @DisplayName("Verbose: tiros válidos e repetidos")
    void testVerboseValidoERepetido() {
        moveWith(miss(), repeated()).processEnemyFire(true);

        assertTrue(consola().contains("1 tiro válido: 1 tiro na água, 1 tiro repetido"));
        assertTrue(consola().contains("1 tiro exterior"));
    }

    @Test
    @DisplayName("Verbose: só tiros exteriores")
    void testVerboseSoExteriores() {
        moveWith().processEnemyFire(true);

        assertTrue(consola().contains("Jogada nº1 -> 3 tiros exteriores"));
    }
}