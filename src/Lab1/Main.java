package Lab1;

import java.util.Arrays;
import java.util.Scanner;

public class Main {

    public static void main(String args[]) {
        Scanner sc = new Scanner(System.in);

        final int max = 1000;
        var p_term = new Polynomial.Term[max];
        var q_term = new Polynomial.Term[max];

        System.out.println("Please Input Polynomial p, '#' for end:");
        int i = 0;
        while (!sc.hasNext("#")) {
            p_term[i++] = new Polynomial.Term(sc.nextDouble(),sc.nextInt());
        }
        p_term = Arrays.copyOfRange(p_term, 0, i);
        var p = new Polynomial(p_term);

        System.out.println("Please Input Polynomial q, '#' for end:");
        sc.next();
        i = 0;
        while (!sc.hasNext("#")) {
            q_term[i++] = new Polynomial.Term(sc.nextDouble(),sc.nextInt());
        }
        q_term = Arrays.copyOfRange(q_term, 0, i);
        var q = new Polynomial(q_term);

        System.out.printf("p   = %s\n", p.toString());
        System.out.printf("q   = %s\n", q.toString());
        System.out.printf("p+q = %s\n", Polynomial.add(p, q));
        System.out.printf("p-q = %s\n", Polynomial.sub(p, q));
        System.out.printf("p*q = %s\n", Polynomial.mult(p, q));
    }

}
