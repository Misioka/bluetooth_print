package android.src.main.java.com.example.bluetooth_print;

import java.util.Locale;

/**
 * Representa un comando en lenguaje CPCL
 * @author drodriguez
 *
 */
public class CpclCommand {
    /**
     * Constante de fin de línea
     */
    private static final String ENDL = "\r\n";
    /**
     * Constante de espacio
     */
    private static final String SP = " ";
    /**
     * Comando de texto
     */
    private static final String TEXT = "T";
    /**
     * Comando de texto multilinea
     */
    private static final String ML = "ML";
    /**
     * Comando de fin de texto multilinea
     */
    private static final String ENDML = "ENDML";
    /**
     * Comando para asignar codificación
     */
    private static final String ENCODING = "ENCODING";
    /**
     * Comando set bold
     */
    private static final String SETBOLD = "SETBOLD";
    /**
     * Comando de definición del espacio entre letras
     */
    private static final String SPACING = "SETSP";
    /**
     * Comando de impresión
     */
    private static final String PRINT = "PRINT";
    /**
     * Comando de Cuadrado
     */
    private static final String BOX = "BOX";
    /**
     * Comando de Linea
     */
    private static final String LINE = "LINE";
    /**
     * Comando de inicio de código QR
     */
    private static final String QR = "B QR";
    /**
     * Comando de fin de código QR
     */
    private static final String ENDQR = "ENDQR";
    /**
     * Comando para impresión de imágen PCX
     */
    private static final String IMAGE = "PCX";
    /**
     * Define las unidades del lenguaje
     * @author drodriguez
     *
     */
    public enum Unit{
        /**
         * Centimetros
         */
        IN_CENTIMETERS("IN-CENTIMETERS"),
        /**
         * Milimetros
         */
        IN_MILIMETERS("IN-MILIMETERS");
        private String valueStr;
        private Unit(String valueStr)
        {	this.valueStr = valueStr;}
        @Override public String toString(){
            return this.valueStr;
        }
    };
    /**
     * Define los tipos de justificación
     * @author drodriguez
     *
     */
    public enum Justify{
        /**
         * Justificado al centro
         */
        CENTER("CENTER"),
        /**
         * Justificado a la izquierda
         */
        LEFT("LEFT"),
        /**
         * Justificado a la derecha
         */
        RIGHT("RIGHT");
        private String valueStr;
        private Justify(String valueStr)
        {	this.valueStr = valueStr;}
        @Override public String toString(){
            return this.valueStr;
        }
    };
    /**
     * Define los tipos de codificación del texto
     * @author drodriguez
     *
     */
    public enum Encoding{
        /**
         * Codificación ASCII
         */
        ASCII("ASCII"),
        /**
         * Codificación UTF-8
         */
        UTF_8("UTF-8");
        private String valueStr;
        private Encoding(String valueStr)
        {	this.valueStr = valueStr;}
        @Override public String toString(){
            return this.valueStr;
        }
    };

    /**
     * Define los tipos de calidades de un código QR
     * @author drodriguez
     *
     */
    public enum QRQuality{
        /**
         * Ultra high reliability level (Level H)
         */
        H("H"),
        /**
         * High reliability level (Level Q)
         */
        Q("Q"),
        /**
         * Standard level (Level M)
         */
        M("M"),
        /**
         * High density level (Level L)
         */
        L("L");

        private String valueStr;
        private QRQuality(String valueStr)
        {	this.valueStr = valueStr;}
        @Override public String toString(){
            return this.valueStr;
        }
    };

    /**
     * La fuente que se asigna con sus getters y setters
     * para usarse en textos sin necesidad de indicar la fuente
     */
    private String font;
    /**
     * El comando que está siendo construido
     */
    private StringBuilder command;
    /**
     * Define el tamaño de la impresión en la unidad deinida
     */
    private double labelHeight;

    /**
     * Inicializa el comando con offset=0 y los valores proporcionados
     * @param horizontalRes resolución en puntos/pulgada
     * @param verticalRes resolución en puntos/pulgada
     * @param height resolución en unidad
     */
    public CpclCommand(int horizontalRes, int verticalRes, double height)
    {
        this(0, horizontalRes, verticalRes, height);
    }

    /**
     * Inicializa el comando con los valores proporcionados
     * @param offset margen que todo el label tendra
     * @param horizontalRes resolución en puntos/pulgada
     * @param verticalRes resolución en puntos/pulgada
     * @param height resolución en unidad
     */
    public CpclCommand(int offset, int horizontalRes, int verticalRes, double height)
    {
        labelHeight = height;
        command = new StringBuilder("! ").append(offset).append(SP).append(horizontalRes)
                .append(SP).append(verticalRes).append(" %s 1\r\n");
    }

    /**
     * Obtiene el tamaño del label definido
     * @return
     */
    public double getLabelHeight() {
        return labelHeight;
    }

    /**
     * Asigna el tamaño que tendrá el label , en la unidad definida
     * @param labelHeight
     */
    public void setLabelHeight(double labelHeight) {
        this.labelHeight = labelHeight;
    }

    /**
     * Asigna la fuente para utilizar en los textos en los que no se necesita definir la fuente
     * @param font la fuente, puede ser una interna o externa que se encuentre en la impresora
     */
    public CpclCommand setFont(String font) {
        this.font = font;
        return this;
    }

    /**
     * Lo que venga despues se tratará en las unidades definidas
     * @param unitType
     * @return la instancia de este comando
     */
    public CpclCommand inUnit(Unit unitType)
    {
        command.append(unitType.toString()).append(ENDL)
                .append("COUNTRY SPAIN").append(ENDL);
        return this;
    }

    /**
     * Justifica el texto que provenga despues
     * @param justifyType
     * @return la instancia de este comando
     */
    public CpclCommand justify(Justify justifyType)
    {
        justify(justifyType, -1);
        return this;
    }

    /**
     * Asigna el tipo de codificación a utilizar
     * @param encodingType
     * @return
     */
    public CpclCommand encoding(Encoding encodingType)
    {
        command.append(ENCODING).append(SP).append(encodingType.toString()).append(ENDL);
        return this;
    }

    /**
     * Justifica el texto que provenga despues
     * @param justifyType
     * @param end el punto donde acaba la justificación, si es <b>-1</b>
     * se utilizará el tamaño del label por defecto
     * @return la instancia de este comando
     */
    public CpclCommand justify(Justify justifyType, double end)
    {
        command.append(justifyType.toString())
                .append(end!=-1?(SP+end):"")
                .append(ENDL);
        return this;
    }

    /**
     * Pone el texto que venga despues al nivel de bold definido, para que el texto sea normal
     * es necesario volver a llamarlo con valor 0
     * @param value valor de bold, en la unidad definida
     * @return la instancia de este comando
     */
    public CpclCommand setBold(double value)
    {
        command.append(SETBOLD).append(SP).append(value).append(ENDL);
        return this;
    }

    /**
     * Pone el texto que venga despues con el espaciado extra entre los caracteres definido
     * es necesario volver a llamarlo con valor 0 para que vuelva el texto a la normalidad
     * @param extraSpacing valor del espaciado extra, en la unidad definida
     * @return la instancia de este comando
     */
    public CpclCommand setSpacing(double extraSpacing)
    {
        command.append(SPACING).append(SP).append(extraSpacing).append(ENDL);
        return this;
    }

    /**
     * Crea un cuadrado en la posición especificada. Tome en cuenta que según la justificación definida
     * se dibuja el cuadrado en la factura, para coordenadas absolutas llame a la justificación LEFT, sin parámetros
     * antes de definir el cuadrado
     * @param leftTopX coordenada X de la punta izquierda superior (en unidad definida)
     * @param leftTopY coordenada Y de la punta izquierda superior (en unidad definida)
     * @param rightBotX coordenada X de la punta derecha inferior (en unidad definida)
     * @param rightBotY coordenada Y de la punta derecha inferior (en unidad definida)
     * @param width el ancho del las lineas del cuadrado, en la unidad definida
     * @return la instancia de este comando
     */
    public CpclCommand box(double leftTopX, double leftTopY, double rightBotX, double rightBotY, double width)
    {
        command.append(BOX).append(SP).append(leftTopX).append(SP).append(leftTopY)
                .append(SP).append(rightBotX).append(SP).append(rightBotY).append(SP).append(width).append(ENDL);
        return this;
    }

    /**
     * Crea una línea en la posición especificada. Tome en cuenta que según la justificación definida
     * se dibuja el cuadrado en la factura, para coordenadas absolutas llame a la justificación LEFT, sin parámetros
     * antes de definir el cuadrado
     * @param startX coordenada X del inicio de la linea (en unidad definida)
     * @param startY coordenada Y del inicio de la linea (en unidad definida)
     * @param endX coordenada X del final de la linea (en unidad definida)
     * @param endY coordenada Y del final de la linea (en unidad definida)
     * @param width el ancho del la linea, en la unidad definida
     * @return la instancia de este comando
     */
    public CpclCommand line(double startX, double startY, double endX, double endY, double width)
    {
        command.append(LINE).append(SP).append(startX).append(SP).append(startY)
                .append(SP).append(endX).append(SP).append(endY).append(SP).append(width).append(ENDL);
        return this;
    }

    /**
     * Agrega un texto para imprimir con la fuente seteada con el método setFont()
     * @param size tamaño de la fuente
     * @param x posición en la factura en la unidad definida
     * @param y posición en la factura en la unidad definida
     * @param text el texto que se quiere imprimir
     * @return la instancia de este comando
     */
    public CpclCommand text(int size, double x, double y, String text)
    {
        if(font==null)
            throw new RuntimeException("Debe definir la fuente con setFont() antes de utilizar el comando text() sin parámetros de fuente");
        text(font, size, x, y, text);
        return this;
    }

    /**
     * Agrega un texto para imprimir con la fuente seteada con el método setFont() usando tamaño 0
     * @param x posición en la factura en la unidad definida
     * @param y posición en la factura en la unidad definida
     * @param text el texto que se quiere imprimir
     * @return la instancia de este comando
     */
    public CpclCommand text(double x, double y, String text)
    {
        text(0, x, y, text);
        return this;
    }

    /**
     * Agrega un texto para imprimir
     * @param innerFont la fuente que se usará para el texto, es una fuente interna de zebra
     * @param size tamaño de la fuente
     * @param x posición en la factura en la unidad definida
     * @param y posición en la factura en la unidad definida
     * @param text el texto que se quiere imprimir
     * @return la instancia de este comando
     */
    public CpclCommand text(int innerFont, int size, double x, double y, String text)
    {
        text(""+innerFont, size, x, y, text);
        return this;
    }

    /**
     * Agrega un texto para imprimir
     * @param font la fuente que se usará para el texto, puede ser una fuente interna
     * o una externa que se encuentra en la impresora
     * @param size tamaño de la fuente
     * @param x posición en la factura en la unidad definida
     * @param y posición en la factura en la unidad definida
     * @param text el texto que se quiere imprimir
     * @return la instancia de este comando
     */
    public CpclCommand text(String font, int size, double x, double y, String text)
    {
        command.append(TEXT).append(SP).append(font).append(SP).append(size)
                .append(SP).append(x).append(SP).append(y).append(SP).append(text).append(ENDL);
        return this;
    }

    /**
     * Agrega un texto para imprimir, con formato de negrilla y extra espaciado definido,
     * esto solo afecta a esta linea de texto
     * @param font la fuente que se usará para el texto, puede ser una fuente interna
     * o una externa que se encuentra en la impresora
     * @param size tamaño de la fuente
     * @param x posición en la factura en la unidad definida
     * @param y posición en la factura en la unidad definida
     * @param boldLevel nivel de bold
     * @param extraSpacing espaciado extra entre caracteres
     * @param text el texto que se quiere imprimir
     * @return la instancia de este comando
     */
    public CpclCommand text(String font, int size, double x, double y, double boldLevel, double extraSpacing, String text)
    {
        setBold(boldLevel);
        setSpacing(extraSpacing);
        text(font, size, x, y, text);
        setBold(0);
        setSpacing(0);
        return this;
    }

    /**
     * Agrega un texto para imprimir, con formato de negrilla y extra espaciado definido,
     * esto solo afecta a esta linea de texto. Utiliza la fuente asignada en setFont()
     * o una externa que se encuentra en la impresora
     * @param size tamaño de la fuente
     * @param x posición en la factura en la unidad definida
     * @param y posición en la factura en la unidad definida
     * @param boldLevel nivel de bold
     * @param extraSpacing espaciado extra entre caracteres
     * @param text el texto que se quiere imprimir
     * @return la instancia de este comando
     */
    public CpclCommand text(int size, double x, double y, double boldLevel, double extraSpacing, String text)
    {
        if(font==null)
            throw new RuntimeException("Debe definir la fuente con setFont() antes de utilizar el comando text() sin parámetros de fuente");
        text(font, size, x, y, boldLevel, extraSpacing, text);
        return this;
    }

    /**
     * Agrega un texto multilinea para imprimir
     * @param lineHeight el alto de cada línea en la unidad definida
     * @param font la fuente que se usará para el texto, puede ser una fuente interna
     * o una externa que se encuentra en la impresora
     * @param size tamaño de la fuente
     * @param x posición en la factura en la unidad definida
     * @param y posición en la factura en las unidad definida
     * @param textLines las líneas de texto a imprimir
     * @return
     */
    public CpclCommand multilineText(double lineHeight, String font, int size, double x, double y, String... textLines)
    {
        command.append(ML).append(SP).append(lineHeight).append(ENDL)
                .append(TEXT).append(SP).append(font).append(SP).append(size).append(SP)
                .append(x).append(SP).append(y).append(ENDL);

        for (int i = 0; i < textLines.length; i++) {
            command.append(textLines[i]).append(ENDL);
        }

        command.append(ENDML).append(ENDL);
        return this;
    }

    /**
     * Agrega un texto multilinea para imprimir utilizando al fuente seteada con setFont
     * @param lineHeight el alto de cada línea en la unidad definida
     * @param size tamaño de la fuente
     * @param x posición en la factura en la unidad definida
     * @param y posición en la factura en las unidad definida
     * @param textLines las líneas de texto a imprimir
     * @return
     */
    public CpclCommand multilineText(double lineHeight, int size, double x, double y, String... textLines)
    {
        if(font==null)
            throw new RuntimeException("Debe definir la fuente con setFont() antes de utilizar el comando text() sin parámetros de fuente");
        multilineText(lineHeight, font, size, x, y, textLines);
        return this;
    }

    /**
     * Grafica un código QR
     * @param size entre 1 y 32
     * @param x la posición X en la unidad definida
     * @param y la posición Y en la unidad definida
     * @param text el texto a codificar
     * @return la instancia de este comando
     */
    public CpclCommand QR(int size, double x, double y, QRQuality quality, String text)
    {
        command.append(QR).append(SP).append(x).append(SP).append(y).append(" U ").append(size).append(ENDL)
                .append(quality.toString()).append(",").append(text).append(ENDL)
                .append(ENDQR).append(ENDL);
        return this;
    }

    /**
     * Imprime una imagen en la posición definida
     * @param x posición en la unidad definida
     * @param y posición en la unidad definida
     * @param imageName nombre de la imagen, con extensión PCX o sin extensión, en caso de pasarse sin extensión
     * se le agregará la extensión .PCX, si se envía con otra extensión se lo tomara como si esta fuera parte del nombre
     * @return la instancia de este comando
     */
    public CpclCommand image(double x, double y, String imageName)
    {
        imageName = imageName.toUpperCase(Locale.getDefault());
        imageName += imageName.contains(".PCX")?"":".PCX";
        command.append(IMAGE).append(SP).append(x).append(SP).append(y).append(" !<")
                .append(imageName).append(ENDL);
        return this;
    }

    /**
     * Añade el comando de impresión
     * @return la instancia de este comando
     */
    public CpclCommand print()
    {
        command.append(PRINT).append(ENDL);
        return this;
    }

    @Override
    public String toString(){
        if(command.lastIndexOf(PRINT)==-1)
            throw new RuntimeException("Debe llamar a print() antes de toString()");
        return String.format(command.toString(), labelHeight);
    }
}
