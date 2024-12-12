package main.java;


class Complejo {
    private double real;
    private double imaginario;

    public Complejo(double real, double imaginario) {
        this.real = real;
        this.imaginario = imaginario;
    }

    public Complejo sumar(Complejo otro) {
        return new Complejo(this.real + otro.real, this.imaginario + otro.imaginario);
    }

    public Complejo restar(Complejo otro) {
        return new Complejo(this.real - otro.real, this.imaginario - otro.imaginario);
    }

    public Complejo multiplicar(Complejo otro) {
        double real = this.real * otro.real - this.imaginario * otro.imaginario;
        double imaginario = this.real * otro.imaginario + this.imaginario * otro.real;
        return new Complejo(real, imaginario);
    }

    public Complejo dividir(Complejo otro) {
        double divisor = otro.real * otro.real + otro.imaginario * otro.imaginario;
        if (divisor == 0) throw new ArithmeticException("División por cero en número complejo");

        double real = (this.real * otro.real + this.imaginario * otro.imaginario) / divisor;
        double imaginario = (this.imaginario * otro.real - this.real * otro.imaginario) / divisor;
        return new Complejo(real, imaginario);
    }

    @Override
public String toString() {
    if (imaginario >= 0) {
        return real + "+" + imaginario + "i";
    } else {
        return real + "" + imaginario + "i"; // No añadimos "+" si imaginario es negativo
    }
}
}
