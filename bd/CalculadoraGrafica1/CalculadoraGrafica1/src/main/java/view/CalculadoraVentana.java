
package view;
import model.*;




public class CalculadoraVentana extends javax.swing.JFrame {

    
    private java.util.ArrayList<Double> numeros = new java.util.ArrayList<>();
    private java.util.ArrayList<String> operadores = new java.util.ArrayList<>();
    private double memoria = 0.0;
    private String operacionPendiente = "";
    private String numeroActual = "";
    private String expresionVisual = "";
    private boolean nuevoNumero = true;

    public CalculadoraVentana() {
    initComponents();

        Operacion.setText("");
        Resultado.setText("");
    }
    
    

    // escribir numero en la jlavbel de operacion
    private void agregarNumero(String digito) {
    numeroActual += digito;
    expresionVisual += digito;
    Operacion.setText(expresionVisual);
    Resultado.setText("");
}

    // proceso operacion basica
    private void procesarOperacion(String nuevaOperacion) {
    try {
        if (!numeroActual.isEmpty()) {
            numeros.add(Double.parseDouble(numeroActual));
            numeroActual = "";
        }
        
        operadores.add(nuevaOperacion);
        expresionVisual += " " + obtenerSimboloVisual(nuevaOperacion) + " ";
        Operacion.setText(expresionVisual);
        Resultado.setText("");
        
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
}

    // fabrica para operaciones basicas
    private model.Operacion obtenerInstanciaOperacion(String tipo) {
        switch (tipo) {
            case "+": return new Suma();
            case "-": return new Resta();
            case "*": return new Multiplicacion();
            case "/": return new Division();
            default: return null;
        }
    }

    // modificacin de los signos
    private String obtenerSimboloVisual(String tipo) {
        switch (tipo) {
            case "+": return "+";
            case "-": return "-";
            case "*": return "×";
            case "/": return "÷";
            default: return "";
        }
    }

    // funciones unitarias de triginometria
    private void ejecutarFuncionUnitaria(model.Operacion operacionModel) {
        try {
            double valor = numeroActual.isEmpty() ? 0.0 : Double.parseDouble(numeroActual);
            double res = operacionModel.calcular(valor);
            
            Operacion.setText(operacionModel.getSimbolo() + "(" + valor + ")");
            Resultado.setText(String.valueOf(res));
            
            expresionVisual = String.valueOf(res);
            numeroActual = String.valueOf(res);
            nuevoNumero = true;
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

   

    
    

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        Resultado = new javax.swing.JLabel();
        Operacion = new javax.swing.JLabel();
        suma = new javax.swing.JButton();
        resta = new javax.swing.JButton();
        multiplicacion = new javax.swing.JButton();
        divison = new javax.swing.JButton();
        cotangente = new javax.swing.JButton();
        uno = new javax.swing.JButton();
        dos = new javax.swing.JButton();
        tres = new javax.swing.JButton();
        seno = new javax.swing.JButton();
        secante = new javax.swing.JButton();
        cuatro = new javax.swing.JButton();
        cinco = new javax.swing.JButton();
        seis = new javax.swing.JButton();
        coseno = new javax.swing.JButton();
        tangente = new javax.swing.JButton();
        siete = new javax.swing.JButton();
        ocho = new javax.swing.JButton();
        nueve = new javax.swing.JButton();
        cosecante = new javax.swing.JButton();
        cero = new javax.swing.JButton();
        igual = new javax.swing.JButton();
        LimpiarTodo = new javax.swing.JButton();
        borrarUno = new javax.swing.JButton();
        Fondo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(520, 500));
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        Resultado.setBackground(new java.awt.Color(102, 102, 102));
        Resultado.setFont(new java.awt.Font("Yu Gothic UI Semilight", 1, 36)); // NOI18N
        Resultado.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.bugtracking.new.color"));
        Resultado.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        Resultado.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Resultado.setOpaque(true);
        getContentPane().add(Resultado, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, 500, 91));

        Operacion.setBackground(new java.awt.Color(102, 102, 102));
        Operacion.setFont(new java.awt.Font("Yu Gothic UI Semilight", 1, 24)); // NOI18N
        Operacion.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.bugtracking.new.color"));
        Operacion.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        Operacion.setOpaque(true);
        getContentPane().add(Operacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 500, 91));

        suma.setBackground(new java.awt.Color(204, 204, 204));
        suma.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.bugtracking.new.color"));
        suma.setText("+");
        suma.setBorder(null);
        suma.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sumaActionPerformed(evt);
            }
        });
        getContentPane().add(suma, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 190, 100, 70));

        resta.setBackground(new java.awt.Color(204, 204, 204));
        resta.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.bugtracking.new.color"));
        resta.setText("-");
        resta.setBorder(null);
        resta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restaActionPerformed(evt);
            }
        });
        getContentPane().add(resta, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 190, 100, 70));

        multiplicacion.setBackground(new java.awt.Color(204, 204, 204));
        multiplicacion.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.bugtracking.new.color"));
        multiplicacion.setText("*");
        multiplicacion.setBorder(null);
        multiplicacion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                multiplicacionActionPerformed(evt);
            }
        });
        getContentPane().add(multiplicacion, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 190, 100, 70));

        divison.setBackground(new java.awt.Color(204, 204, 204));
        divison.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.bugtracking.new.color"));
        divison.setText("/");
        divison.setBorder(null);
        divison.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                divisonActionPerformed(evt);
            }
        });
        getContentPane().add(divison, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 190, 100, 70));

        cotangente.setBackground(new java.awt.Color(204, 204, 204));
        cotangente.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.html.link.foreground.hover"));
        cotangente.setText("Cota");
        cotangente.setBorder(null);
        cotangente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cotangenteActionPerformed(evt);
            }
        });
        getContentPane().add(cotangente, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 190, 100, 70));

        uno.setBackground(new java.awt.Color(204, 204, 204));
        uno.setForeground(new java.awt.Color(102, 102, 102));
        uno.setText("1");
        uno.setBorder(null);
        uno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unoActionPerformed(evt);
            }
        });
        getContentPane().add(uno, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 260, 100, 70));

        dos.setBackground(new java.awt.Color(204, 204, 204));
        dos.setForeground(new java.awt.Color(102, 102, 102));
        dos.setText("2");
        dos.setBorder(null);
        dos.setOpaque(true);
        dos.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dosActionPerformed(evt);
            }
        });
        getContentPane().add(dos, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 260, 100, 70));

        tres.setBackground(new java.awt.Color(204, 204, 204));
        tres.setForeground(new java.awt.Color(102, 102, 102));
        tres.setText("3");
        tres.setBorder(null);
        tres.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tresActionPerformed(evt);
            }
        });
        getContentPane().add(tres, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 260, 100, 70));

        seno.setBackground(new java.awt.Color(204, 204, 204));
        seno.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.html.link.foreground.hover"));
        seno.setText("Sin");
        seno.setBorder(null);
        seno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                senoActionPerformed(evt);
            }
        });
        getContentPane().add(seno, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 260, 100, 70));

        secante.setBackground(new java.awt.Color(204, 204, 204));
        secante.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.html.link.foreground.hover"));
        secante.setText("Seca");
        secante.setBorder(null);
        secante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secanteActionPerformed(evt);
            }
        });
        getContentPane().add(secante, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 260, 100, 70));

        cuatro.setBackground(new java.awt.Color(204, 204, 204));
        cuatro.setForeground(new java.awt.Color(102, 102, 102));
        cuatro.setText("4");
        cuatro.setBorder(null);
        cuatro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cuatroActionPerformed(evt);
            }
        });
        getContentPane().add(cuatro, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 330, 100, 70));

        cinco.setBackground(new java.awt.Color(204, 204, 204));
        cinco.setForeground(new java.awt.Color(102, 102, 102));
        cinco.setText("5");
        cinco.setBorder(null);
        cinco.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cincoActionPerformed(evt);
            }
        });
        getContentPane().add(cinco, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 330, 100, 70));

        seis.setBackground(new java.awt.Color(204, 204, 204));
        seis.setForeground(new java.awt.Color(102, 102, 102));
        seis.setText("6");
        seis.setBorder(null);
        seis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                seisActionPerformed(evt);
            }
        });
        getContentPane().add(seis, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 330, 100, 70));

        coseno.setBackground(new java.awt.Color(204, 204, 204));
        coseno.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.html.link.foreground.hover"));
        coseno.setText("Cos");
        coseno.setBorder(null);
        coseno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cosenoActionPerformed(evt);
            }
        });
        getContentPane().add(coseno, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 330, 100, 70));

        tangente.setBackground(new java.awt.Color(204, 204, 204));
        tangente.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.html.link.foreground.hover"));
        tangente.setText("Tan");
        tangente.setBorder(null);
        tangente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tangenteActionPerformed(evt);
            }
        });
        getContentPane().add(tangente, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 330, 100, 70));

        siete.setBackground(new java.awt.Color(204, 204, 204));
        siete.setForeground(new java.awt.Color(102, 102, 102));
        siete.setText("7");
        siete.setBorder(null);
        siete.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sieteActionPerformed(evt);
            }
        });
        getContentPane().add(siete, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 400, 100, 70));

        ocho.setBackground(new java.awt.Color(204, 204, 204));
        ocho.setForeground(new java.awt.Color(102, 102, 102));
        ocho.setText("8");
        ocho.setBorder(null);
        ocho.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ochoActionPerformed(evt);
            }
        });
        getContentPane().add(ocho, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 400, 100, 70));

        nueve.setBackground(new java.awt.Color(204, 204, 204));
        nueve.setForeground(new java.awt.Color(102, 102, 102));
        nueve.setText("9");
        nueve.setBorder(null);
        nueve.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nueveActionPerformed(evt);
            }
        });
        getContentPane().add(nueve, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 400, 100, 70));

        cosecante.setBackground(new java.awt.Color(204, 204, 204));
        cosecante.setForeground(javax.swing.UIManager.getDefaults().getColor("nb.html.link.foreground.hover"));
        cosecante.setText("Csec");
        cosecante.setBorder(null);
        cosecante.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cosecanteActionPerformed(evt);
            }
        });
        getContentPane().add(cosecante, new org.netbeans.lib.awtextra.AbsoluteConstraints(300, 400, 100, 70));

        cero.setBackground(new java.awt.Color(204, 204, 204));
        cero.setForeground(new java.awt.Color(102, 102, 102));
        cero.setText("0");
        cero.setBorder(null);
        cero.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ceroActionPerformed(evt);
            }
        });
        getContentPane().add(cero, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 470, 200, 70));

        igual.setBackground(new java.awt.Color(204, 204, 204));
        igual.setForeground(javax.swing.UIManager.getDefaults().getColor("Objects.Red"));
        igual.setText("=");
        igual.setBorder(null);
        igual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                igualActionPerformed(evt);
            }
        });
        getContentPane().add(igual, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 470, 200, 70));

        LimpiarTodo.setBackground(new java.awt.Color(204, 204, 204));
        LimpiarTodo.setForeground(new java.awt.Color(255, 102, 102));
        LimpiarTodo.setText("AC");
        LimpiarTodo.setBorder(null);
        LimpiarTodo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                LimpiarTodoActionPerformed(evt);
            }
        });
        getContentPane().add(LimpiarTodo, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 470, 100, 70));

        borrarUno.setBackground(new java.awt.Color(204, 204, 204));
        borrarUno.setForeground(new java.awt.Color(255, 102, 102));
        borrarUno.setText("<--");
        borrarUno.setBorder(null);
        borrarUno.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                borrarUnoActionPerformed(evt);
            }
        });
        getContentPane().add(borrarUno, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 400, 100, 70));

        Fondo.setBackground(new java.awt.Color(204, 204, 204));
        Fondo.setOpaque(true);
        getContentPane().add(Fondo, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 500, 550));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cotangenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cotangenteActionPerformed
        ejecutarFuncionUnitaria(new Cotangente());
    }//GEN-LAST:event_cotangenteActionPerformed

    private void multiplicacionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_multiplicacionActionPerformed
        procesarOperacion("*");
    }//GEN-LAST:event_multiplicacionActionPerformed

    private void tangenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tangenteActionPerformed
        ejecutarFuncionUnitaria(new Tangente());
    }//GEN-LAST:event_tangenteActionPerformed

    private void cosenoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cosenoActionPerformed
        ejecutarFuncionUnitaria(new Coseno());
    }//GEN-LAST:event_cosenoActionPerformed

    private void secanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secanteActionPerformed
        ejecutarFuncionUnitaria(new Secante());
    }//GEN-LAST:event_secanteActionPerformed

    private void cosecanteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cosecanteActionPerformed
        ejecutarFuncionUnitaria(new Cosecante());
    }//GEN-LAST:event_cosecanteActionPerformed

    private void restaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restaActionPerformed
        procesarOperacion("-");
    }//GEN-LAST:event_restaActionPerformed

    private void igualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_igualActionPerformed
        try {
        if (!numeroActual.isEmpty()) {
            numeros.add(Double.parseDouble(numeroActual));
            numeroActual = "";
        }
        
        if (numeros.isEmpty()) return;
        java.util.ArrayList<Double> nums = new java.util.ArrayList<>(numeros);
        java.util.ArrayList<String> ops = new java.util.ArrayList<>(operadores);
        
        // multiplicaiones y divisiones
        for (int i = 0; i < ops.size(); i++) {
            String op = ops.get(i);
            if (op.equals("*") || op.equals("/")) {
                double n1 = nums.get(i);
                double n2 = nums.get(i + 1);
                double res = 0.0;
                
                if (op.equals("*")) {
                    res = new Multiplicacion().calcular(n1, n2);
                } else {
                    res = new Division().calcular(n1, n2);
                }
                
                nums.set(i, res);
                nums.remove(i + 1);
                ops.remove(i);
                i--; 
            }
        }
        
        // Sumas y Restas
        double resultadoFinal = nums.get(0);
        for (int i = 0; i < ops.size(); i++) {
            String op = ops.get(i);
            double n2 = nums.get(i + 1);
            
            if (op.equals("+")) {
                resultadoFinal = new Suma().calcular(resultadoFinal, n2);
            } else if (op.equals("-")) {
                resultadoFinal = new Resta().calcular(resultadoFinal, n2);
            }
        }
        
        Operacion.setText(expresionVisual + " =");
        Resultado.setText(String.valueOf(resultadoFinal));
        
        // se limpia si el usuario quiere seguir operando 
        numeros.clear();
        operadores.clear();
        numeros.add(resultadoFinal);
        expresionVisual = String.valueOf(resultadoFinal);
        numeroActual = "";
        
    } catch (Exception e) {
        javax.swing.JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
    }
    }//GEN-LAST:event_igualActionPerformed

    private void divisonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_divisonActionPerformed
        procesarOperacion("/");
    }//GEN-LAST:event_divisonActionPerformed

    private void senoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_senoActionPerformed
        ejecutarFuncionUnitaria(new Seno());
    }//GEN-LAST:event_senoActionPerformed

    private void dosActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dosActionPerformed
        agregarNumero("2");
    }//GEN-LAST:event_dosActionPerformed

    private void sieteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sieteActionPerformed
        agregarNumero("7");
    }//GEN-LAST:event_sieteActionPerformed

    private void seisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_seisActionPerformed
        agregarNumero("6");
    }//GEN-LAST:event_seisActionPerformed

    private void cincoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cincoActionPerformed
        agregarNumero("5");
    }//GEN-LAST:event_cincoActionPerformed

    private void unoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unoActionPerformed
        agregarNumero("1");
    }//GEN-LAST:event_unoActionPerformed

    private void sumaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sumaActionPerformed
        procesarOperacion("+");
    }//GEN-LAST:event_sumaActionPerformed

    private void cuatroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cuatroActionPerformed
        agregarNumero("4");
    }//GEN-LAST:event_cuatroActionPerformed

    private void tresActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tresActionPerformed
        agregarNumero("3");
    }//GEN-LAST:event_tresActionPerformed

    private void ochoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ochoActionPerformed
        agregarNumero("7");
    }//GEN-LAST:event_ochoActionPerformed

    private void ceroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ceroActionPerformed
        agregarNumero("0");
    }//GEN-LAST:event_ceroActionPerformed

    private void nueveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nueveActionPerformed
        agregarNumero("9");
    }//GEN-LAST:event_nueveActionPerformed

    private void LimpiarTodoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_LimpiarTodoActionPerformed
    numeros.clear();
    operadores.clear();
    numeroActual = "";
    expresionVisual = "";
    Operacion.setText("");
    Resultado.setText("");
    
    
    }//GEN-LAST:event_LimpiarTodoActionPerformed

    private void borrarUnoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_borrarUnoActionPerformed
        if (!expresionVisual.isEmpty()) {
        expresionVisual = expresionVisual.substring(0, expresionVisual.length() - 1);
        if (!numeroActual.isEmpty()) {
            numeroActual = numeroActual.substring(0, numeroActual.length() - 1);
        }
        Operacion.setText(expresionVisual);
        Resultado.setText("");
    }
    }//GEN-LAST:event_borrarUnoActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CalculadoraVentana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CalculadoraVentana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CalculadoraVentana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CalculadoraVentana.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CalculadoraVentana().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel Fondo;
    private javax.swing.JButton LimpiarTodo;
    private javax.swing.JLabel Operacion;
    private javax.swing.JLabel Resultado;
    private javax.swing.JButton borrarUno;
    private javax.swing.JButton cero;
    private javax.swing.JButton cinco;
    private javax.swing.JButton cosecante;
    private javax.swing.JButton coseno;
    private javax.swing.JButton cotangente;
    private javax.swing.JButton cuatro;
    private javax.swing.JButton divison;
    private javax.swing.JButton dos;
    private javax.swing.JButton igual;
    private javax.swing.JButton multiplicacion;
    private javax.swing.JButton nueve;
    private javax.swing.JButton ocho;
    private javax.swing.JButton resta;
    private javax.swing.JButton secante;
    private javax.swing.JButton seis;
    private javax.swing.JButton seno;
    private javax.swing.JButton siete;
    private javax.swing.JButton suma;
    private javax.swing.JButton tangente;
    private javax.swing.JButton tres;
    private javax.swing.JButton uno;
    // End of variables declaration//GEN-END:variables
}
