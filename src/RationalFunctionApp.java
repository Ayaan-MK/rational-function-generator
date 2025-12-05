import javax.swing.SwingUtilities;

public class RationalFunctionApp {
	
    public static void main(String[] args) {
    	
        SwingUtilities.invokeLater ( () -> {
            RationalFunctionFrame frame = new RationalFunctionFrame();
            frame.setVisible(true);
            
        } );
    }
}
