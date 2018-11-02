package Lab1;

import List.LinkedList;

import java.util.Arrays;
import java.util.Comparator;

public class Polynomial {

    public static class Term {
        private int index;
        private double coefficient;

        public Term(double coefficient,int index) {
            this.index = index;
            this.coefficient = coefficient;
        }

        public int getIndex() {
            return index;
        }

        public double getCoefficient() {
            return coefficient;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public void setCoefficient(double coefficient) {
            this.coefficient = coefficient;
        }

        public void addSelf(Term a) {
            coefficient += a.coefficient;
        }

        public static Term add(Term a, Term b) {
            if (a.index != b.index) {
                throw new IllegalArgumentException();
            }
            return new Term(a.coefficient + b.coefficient,a.index);
        }

        public static Term sub(Term a, Term b) {
            if (a.index != b.index) {
                throw new IllegalArgumentException();
            }
            return new Term(a.coefficient - b.coefficient,a.index);
        }

        public static Term mult(Term a, Term b) {
            return new Term(
                    a.coefficient * b.coefficient,a.index + b.index);
        }

        public static int compareIndex(Term a, Term b) {
            int d = a.getIndex() - b.getIndex();
            if (d == 0) {
                return 0;
            } else if (d > 0) {
                return 1;
            } else {
                return -1;
            }
        }

        @Override
        public String toString() {
            if (coefficient == 0.0) {
                return "";
            } else if (coefficient == 1.0) {
                switch (index) {
                    case 0:
                        return " + 1.0";
                    case 1:
                        return " + x";
                    default:
                        return " + x^" + index;
                }
            } else if (coefficient == -1.0) {
                switch (index) {
                    case 0:
                        return " - 1.0";
                    case 1:
                        return " - x";
                    default:
                        return " - x^" + index;
                }
            } else {
                var sb = new StringBuilder();
                if (coefficient > 0.0) {
                    switch (index) {
                        case 0:
                            return " + " + coefficient;
                        case 1:
                            return " + " + coefficient + " x";
                        default:
                            return " + " + coefficient + " x^" + index;
                    }
                } else {
                    switch (index) {
                        case 0:
                            return " - " + (-coefficient);
                        case 1:
                            return " - " + (-coefficient) + " x";
                        default:
                            return " - " + (-coefficient) + " x^" + index;
                    }
                }
            }
        }


    }

    private LinkedList<Term> terms;

    public Polynomial(Term[] t) {
        //按指数升序排列
        Arrays.sort(t, new Comparator<Term>() {
            @Override
            public int compare(Term o1, Term o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });
        terms = new LinkedList<>(t);

        //去重
        var p = terms.getHeadNode().getNext();
        while (p != null) {
            var q = p.getNext();
            while (q != null && p.getData().getIndex() == q.getData().getIndex()) {
                q = q.getNext();
            }
            //合并从p到q.pre
            var r = p.getNext();
            while (r != q) {
                p.getData().addSelf(r.getData());
                r = r.getNext();
            }
            p.setNext(q);
            p = q;
        }
    }

    private Polynomial(LinkedList<Term> terms) {
        this.terms = terms;
    }

    public static Polynomial add(Polynomial p, Polynomial q) {
        return add_and_sub(p, q, true);
    }

    public static Polynomial sub(Polynomial p, Polynomial q) {
        return add_and_sub(p, q, false);
    }

    private static Polynomial add_and_sub(Polynomial p, Polynomial q, boolean mode) {
        var result = new LinkedList<Term>();

        var iter1 = p.terms.iterator();
        var iter2 = q.terms.iterator();

        Term a = null, b = null;

        for (; ; ) {
            if (a == null && iter1.hasNext()) {
                a = iter1.next();
            }
            if (b == null && iter2.hasNext()) {
                b = iter2.next();
            }
            if (a == null || b == null) {
                if (a != null) {
                    result.insertTail(a);
                    while (iter1.hasNext()) {
                        result.insertTail(iter1.next());
                    }
                } else if (b != null) {
                    result.insertTail(b);
                    while (iter2.hasNext()) {
                        result.insertTail(iter2.next());
                    }
                }
                break;
            }

            switch (Term.compareIndex(a, b)) {
                case 0:
                    result.insertTail(mode ? Term.add(a, b) : Term.sub(a, b));
                    a = null;
                    b = null;
                    break;
                case 1:
                    result.insertTail(b);
                    b = null;
                    break;
                case -1:
                    result.insertTail(a);
                    a = null;
                    break;
                default:
            }
        }
        return new Polynomial(result);
    }

    public static Polynomial mult(Polynomial p, Polynomial q) {

        Term[] tmp = new Term[p.terms.getLength() * q.terms.getLength()];
        var iter1 = p.terms.iterator();
        var iter2 = q.terms.iterator();
        int i = 0;
        while (iter1.hasNext()) {
            var term1 = iter1.next();
            while (iter2.hasNext()) {
                tmp[i++] = Term.mult(term1, iter2.next());
            }
            iter2.reset();
        }
        return new Polynomial(tmp);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        var iter = terms.iterator();
        while (iter.hasNext()) {
            sb.append(iter.next().toString());
        }
        if (sb.length() == 0) {
            sb.append(" 0.0");
        }
        sb.deleteCharAt(0);
        if (sb.charAt(0) == '+') {
            sb.delete(0, 2);
        }
        return sb.toString();
    }
}
