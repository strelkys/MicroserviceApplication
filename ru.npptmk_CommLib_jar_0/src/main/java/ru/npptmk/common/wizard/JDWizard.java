package ru.npptmk.common.wizard;

import java.util.ArrayList;
import javax.swing.JOptionPane;

/**
 * Класс предназначен для создания мастера настройки параметров оборудования при
 * переходе из одного режима в другой.<br>
 * Класс содержит методы обеспечивающие взаимодействие основного ДИАЛОГА с
 * панелями, взаимодействие панелей друг с другом и обратной связи панелей с
 * основным ДИАЛОГОМ. Клас выполнен в виде ДИАЛОГА [JDialog] которому передаются
 * панели [JPanel] для организации ДИАЛОГА между пользователем, программой и
 * устройством. Панели имеют структуру интерфейса IWstep. Количество,
 * содержание, алгоритмы работы панелей определяются каждым конкретным
 * устройством в зависимости от его технологического процесса.
 */
public class JDWizard extends javax.swing.JDialog {

// Массив панелей используемых в ДИАЛОГЕ. Передается в кострукот класса для возможности работы с ними в классе.
    private final IWstep[] steps;
    private int currStep;
// Создаем коллекция индексов панелей.
    private final ArrayList<Integer> listSteps = new ArrayList();
    private boolean state;

    /**
     * Creates new form JDialogW
     *
     * @param steps
     */
    public JDWizard(IWstep[] steps) {
        super((java.awt.Frame) null, false);
        this.steps = steps;
        initComponents();
        setLocationRelativeTo(null);
    }

    /**
     * Возвращает индекс текущего этапа мастера.
     *
     * @return Индекс текущего этапа мастера.
     */
    public int getCurrStep() {
        return currStep;
    }

    /**
     * Возвращает статус завершения мастера.
     *
     * @return {@code true} - если причиной завершения мастера стало значение -1
     * возвращенное методом {@code next()} последнего этапа. <br> {@code false}
     * - если мастер завершился в результате нажатия пользователем кнопки
     * "Отмена".
     */
    public boolean getState() {
        return state;
    }

    /**
     * Определяет видимость кнопки "вперед" в ДИАЛОГЕ.
     *
     * @param press входной параметр определяет видимость кнопки.
     */
    public void setFwd(boolean press) {
        tbForvard.setVisible(press);
    }

    /**
     * Определяет текстовое значение кнопки.
     *
     * @param text текстовое значение.
     */
    public void setFwdText(String text) {
        tbForvard.setText(text);
    }

    public void setExit(boolean value) {
        tbExit.setVisible(value);
    }

    public void setBack(boolean value) {
        tbBack.setVisible(value);
    }

    /**
     *
     * По индексу панели возвращает объект.
     *
     * @param panel индекс панели.
     * @return возвращает объект типа IWstep.
     */
    public IWstep getPanel(int panel) {
        return steps[panel];
    }

    /**
     * Метод вызываемый для начала использования мастера настройки.<br>
     * Устанавливает начальные настройки и значения переменных.Отображает первую
     * панель ДИАЛОГА. Далее идет последовательный вызов всех панелей,
     * необходимых для настройки параметров перед работой.
     *
     * @param s значение оглавления ДИАЛОГА
     */
    public void startWiz(String s) {
        listSteps.clear(); // Очищаем массив индексов панелей
        currStep = 0;
        state = false;
        showStep();
        setTitle(s);
        setVisible(true);
    }

    /**
     * Метод отображает очередную панель в зависимости от значения переменной
     * currStep. Переменная currStep получает свое значение из текущей панели
     * как результат выполнения метода {@code next()}.
     *
     */
    private void showStep() {
// Значение переменной currStep должно быть в диапазоне положительных чисел.
        if (currStep == -1) {
// Переменная определяющая статус завершения работаы мастера принимает значение true в случае штатного завершения работы всех панелей.
            state = true;
// Закрываем ДИАЛОГ, завершаем работу мастера.           
            setVisible(false);
            return;
        }
//Проверяем входит ли значение переменной currStep в массив индексов панелей.        
        if (currStep >= steps.length) {
            JOptionPane.showMessageDialog(null, "Текущий шаг мастера настройки выходит за размерность массива steps");
            setVisible(false);
            return;
        }
        IWstep cs = steps[currStep]; // Выбираем объект/панель с полученным из предидущей панели индексом.
        setFwdText("Вперед"); //Определяем текстовое значение кнопки в случае если в вызываемой панели этоне предусмотренно.
        cs.setup(this); //Метод начальных настроек выбранной панели.
        currPanel.setVisible(false);
        currPanel.removeAll(); //Подготавливаем окно ДИАЛОГА к отображению выбранной панели.
        currPanel.add(cs.getPanel()); //Отображаем выбранную панель в окне ДИАЛОГА.
        currPanel.setVisible(true);
        pack();
    }

    /**
     * Команда перейти к следующему этапу мастера.<br>
     * Этот метод производит вызом метода next() текущей панели и переход к
     * панели, индекс которой содержится в его результате.<br>
     * Метод должен быть вызван из потока обработки очереди событий Swing. В
     * случае вызова данного метода из другого потока используйте
     * {@code EventQueue.invokeLater()}.
     */
    public void goFwd() {
        listSteps.add(currStep);
        currStep = steps[currStep].next();
        showStep();
    }

    /**
     * Команда возврата к предыдущему этапу пастера<br>
     * Этот метод извлекает из коллекции переходов предыдущий этап мастера и
     * осуществоляет возврат к нему.<br>
     * Метод должен быть вызван из потока обработки очереди событий Swing. В
     * случае вызова данного метода из другого потока используйте
     * {@code EventQueue.invokeLater()}.
     */
    public void goRev() {
//Если массив индексов панелей пустой, устанавливаем значение переменной currStep = 0,
//если нет устанавливаем последнее последнего индекса массива. 
        if (listSteps.isEmpty()) {
            currStep = 0;
        } else {
            currStep = listSteps.remove(listSteps.size() - 1);
        }
        showStep();
    }

    /**
     * Завершение выполнения мастера.<br>
     */
    public void stopWiz() {
        setVisible(false);
    }

    /**
     * Статический метод для создания и запуска на выполнение мастера.<br>
     * Метод должен быть вызван из потока обработки очереди событий Swing. В
     * случае вызова данного метода из другого потока используйте
     * {@code EventQueue.invokeLater()}.
     *
     * @param stp массив этапов мастера
     * @param title заголовок диалога мастера
     */
    public static void doWizard(IWstep[] stp, String title) {
        JDWizard dlg = new JDWizard(stp);
        dlg.startWiz(title);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        tbBack = new javax.swing.JButton();
        tbExit = new javax.swing.JButton();
        tbForvard = new javax.swing.JButton();
        currPanel = new javax.swing.JPanel();

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Панель 2");
        jLabel2.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 534, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(142, 142, 142)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(142, Short.MAX_VALUE)))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 414, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel2Layout.createSequentialGroup()
                    .addGap(138, 138, 138)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(139, Short.MAX_VALUE)))
        );

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Панель 1");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(171, 171, 171)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(181, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(122, 122, 122)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(132, Short.MAX_VALUE))
        );

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Панель 3");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(220, Short.MAX_VALUE)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(205, 205, 205))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(127, 127, 127)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(145, Short.MAX_VALUE))
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setAlwaysOnTop(true);
        setMaximumSize(new java.awt.Dimension(640, 480));
        setMinimumSize(new java.awt.Dimension(640, 480));
        setPreferredSize(new java.awt.Dimension(640, 480));
        setResizable(false);
        setSize(new java.awt.Dimension(640, 480));

        tbBack.setText("Назад");
        tbBack.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbBackActionPerformed(evt);
            }
        });

        tbExit.setText("Отмена");
        tbExit.setToolTipText("");
        tbExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbExitActionPerformed(evt);
            }
        });

        tbForvard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tbForvardActionPerformed(evt);
            }
        });

        currPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(currPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tbBack)
                .addGap(72, 72, 72)
                .addComponent(tbExit)
                .addGap(77, 77, 77)
                .addComponent(tbForvard, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(261, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(currPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 440, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tbForvard, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(tbExit)
                        .addComponent(tbBack)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tbForvardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbForvardActionPerformed
        goFwd();
    }//GEN-LAST:event_tbForvardActionPerformed

    private void tbBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbBackActionPerformed
        goRev();
    }//GEN-LAST:event_tbBackActionPerformed

    private void tbExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tbExitActionPerformed
        setVisible(false);
    }//GEN-LAST:event_tbExitActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel currPanel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JButton tbBack;
    private javax.swing.JButton tbExit;
    private javax.swing.JButton tbForvard;
    // End of variables declaration//GEN-END:variables
}
