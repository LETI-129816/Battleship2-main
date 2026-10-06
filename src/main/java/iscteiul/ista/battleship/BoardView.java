package iscteiul.ista.battleship;
import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;


public class BoardView  extends JFrame {

    private static final int SIZE = 10;
    private final JButton[][] gridButtons = new JButton[SIZE][SIZE];

    public BoardView() {

        //Inicializar um tema escuro moderno na biblioteca FlatLaf (User story #4)
        FlatDarkLaf.setup();

        setTitle("Batalha Naval dos Descobrimentos - Vista do Tabuleiro");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(650,650);
        setLocationRelativeTo(null);

        initUI();

    }

    private void initUI(){

        JPanel mainPanel = new JPanel (new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));

        JLabel titleLabel = new JLabel("Tabuleiro de Jogo (Greelha 10x10)",SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

       //Grelha 11x11 para inicializar as coordenadas (A-J e 1-10)
        JPanel boardPanel = new JPanel(new GridLayout(SIZE + 1, SIZE + 1, 3,3));

        //Canto Superior esquerdo vazio
        boardPanel.add(new JLabel("", SwingConstants.CENTER));

        //Cabeçalhos das colunas ( 1 a 10)
        for (int col = 1; col <= SIZE; col++){
            JLabel colHeader = new JLabel(String.valueOf(col), SwingConstants.CENTER);
            colHeader.setFont(new Font("SansSerif", Font.BOLD, 14));
            boardPanel.add(colHeader);
        }

        //Linhas (A - J)
        for(int row = 0; row < SIZE; row++){
            char rowChar =(char)('A' + row);
            JLabel rowHeader = new JLabel(String.valueOf(rowChar), SwingConstants.CENTER);
            rowHeader.setFont(new Font("SansSerif",Font.BOLD,14));
            boardPanel.add(rowHeader);

            for (int col = 0; col < SIZE; col++){
                JButton cell = new JButton("~");
                cell.setFont( new Font("Monospaced", Font.PLAIN, 12));
                cell.setFocusPainted(false);
                cell.setBackground(new Color(25,42,60)); //Tom de água

                final String pos = "" + rowChar + (col + 1);
                cell.addActionListener(e -> {
                    cell.setText("X");
                    cell.setBackground(new Color(160, 40, 40)); //Marcador de tiro
                    JOptionPane.showMessageDialog(this, "Coordenada selecionada " + pos);
                });

                gridButtons[row][col] = cell;
                boardPanel.add(cell);

            }
        }

        mainPanel.add(boardPanel, BorderLayout.CENTER);
        setContentPane(mainPanel);
    }

    public static void showBoard(){
        SwingUtilities.invokeLater(() ->{
            BoardView view = new BoardView();
            view.setVisible(true);
        });
    }

    public static void main(String[] args){
        showBoard();
    }

}
