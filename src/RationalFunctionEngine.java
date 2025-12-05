import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

public class RationalFunctionEngine {
	
    // ====== public api ======
    
    
    public static class Poly {
    	
        public final ArrayList<Integer> coeffs; // coeffs, low degree first
        public final ArrayList<Integer> roots;  // roots (with repeats)
        public final int leading;               // leading coeff
        
        
        public Poly
        (ArrayList<Integer> coeffs,ArrayList<Integer> roots) {
        	
            this.coeffs = trimZeros(coeffs);
            this.roots = new ArrayList<>(roots);
            
            if (this.coeffs.isEmpty()) {
            	this.leading = 0;
            }
            else {
            	this.leading = this.coeffs.get(this.coeffs.size() - 1);
            }
        }
        
        
        public int degree() {
        	// degree is last index
            return coeffs.size() - 1;
        }
        
        
        public String toStandardString() {
        	
            if (coeffs.isEmpty()) return "0";
            
            String s = "";
            
            // go from highest power down
            for (int d = degree(); d >= 0; d--) {
            	
                int c = coeffs.get(d);
                if (c == 0) continue; // skip zero terms
                
                int abs = Math.abs(c);
                String term;
                
                if (d == 0) {
                    term = Integer.toString(abs);
                }
                else if (d == 1) {
                    term = (abs == 1 ? "x" : abs + "x");
                }
                else {
                    term = (abs == 1 ? "x^" + d : abs + "x^" + d);
                }
                
                if (s.isEmpty()) {
                	// first term
                    if (c < 0) s += "-";
                    s += term;
                }
                else {
                	// later terms with + or -
                    s += (c < 0 ? " - " : " + ");
                    s += term;
                }
            }
            
            return s.isEmpty() ? "0" : s;
        }
        
        
        public String toFactorString() {
        	
            if (roots.isEmpty()) return Integer.toString(leading);
            
            StringBuilder sb = new StringBuilder();
            sb.append(leading);
            
            // build like a(x-r1)(x-r2)...
            for (int r : roots) {
            	
                sb.append("(x");
                if (r >= 0) sb.append("-").append(r);
                else sb.append("+").append(-r);
                sb.append(")");
            }
            
            return sb.toString();
        }
        
        
        public String rootsWithMultiplicity() {
        	
            if (roots.isEmpty()) return "none";
            
            StringBuilder sb = new StringBuilder();
            ArrayList<Integer> seen = new ArrayList<>();
            
            // group same roots and count them
            for (int r : roots) {
            	
                if (seen.contains(r)) continue; // already counted
                
                int count = 0;
                for (int root : roots) {
                    if (root == r) count++;
                }
                
                if (sb.length() > 0) sb.append(", ");
                sb.append(r);
                
                if (count > 1) {
                    sb.append(" (mult ").append(count).append(")");
                }
                
                seen.add(r);
            }
            
            return sb.toString();
        }
    }
    
    
    public static class RationalFunction {
    	
        public final Poly numer; // numerator
        public final Poly denom; // denominator
        
        
        public RationalFunction
        (Poly n,Poly d) {
        	
            this.numer = n;
            this.denom = d;
        }
        
        
        public double valueAt(double x) {
        	
            double num = 0;
            double den = 0;
            double xp = 1.0;
            
            // evaluate numerator by powers of x
            for (int c : numer.coeffs) {
                num += c * xp;
                xp *= x;
            }
            
            xp = 1.0;
            // evaluate denominator by powers of x
            for (int c : denom.coeffs) {
                den += c * xp;
                xp *= x;
            }
            
            return num / den;
        }
    }
    
    
    public static RationalFunction generateRational
    (int maxDegree,String difficulty,Random rng) {
    	
        // pick degrees for n and d
        int degD = randInt(rng,1,maxDegree - 1);
        int degN = degD + 1;
        
        // avoid both at maxDegree
        if (degN == maxDegree && degD == maxDegree) {
            if (rng.nextBoolean()) degN--;
            else degD--;
        }
        
        int rootAbs = 6;                 // roots in [-6,6]
        boolean allowMult = !difficulty.equals("EASY");
        
        // random roots for numerator and denominator
        ArrayList<Integer> rootsN = randomRootsWithMultiplicity
        (degN,rootAbs,allowMult,rng);
        
        ArrayList<Integer> rootsD = randomRootsWithMultiplicity
        (degD,rootAbs,allowMult,rng);
        
        // use set for quick check of shared roots
        HashSet<Integer> numerRootSet = new HashSet<>(rootsN);
        
        // try to avoid shared roots between n and d
        for (int i = 0; i < rootsD.size(); i++) {
        	
            int guard = 0;
            int current = rootsD.get(i);
            
            while (numerRootSet.contains(current) && guard < 20) {
                current = randomNonZeroDifferent
                (current,rootAbs,numerRootSet,rng);
                guard++;
            }
            
            rootsD.set(i,current);
        }
        
        // final check for shared roots (if still same, give up)
        for (int r : rootsD) {
            if (numerRootSet.contains(r)) return null;
        }
        
        // choose leading coeffs
        int leadN;
        int leadD;
        
        if ("HARD".equals(difficulty)) {
        	
        	// hard mode: force non ±1 leading coeffs
        	leadN = pickNonUnitLeading(rng);
        	leadD = pickNonUnitLeading(rng);
        }
        else {
        	
        	// easy/medium: allow ±1, ±2, ±3 but not 0
            leadN = randInt(rng,-3,3);
            leadD = randInt(rng,-3,3);
            
            if (leadN == 0) leadN = 1;
            if (leadD == 0) leadD = 1;
        }
        
        // expand roots to coeffs
        ArrayList<Integer> coeffN = expandFromRoots(rootsN,leadN);
        ArrayList<Integer> coeffD = expandFromRoots(rootsD,leadD);
        
        // simplify by gcd if possible
        int gN = gcdList(coeffN);
        int gD = gcdList(coeffD);
        
        // only divide if it does not break hard-mode leading coeff
        if (gN > 1) {
        	
        	int newLeadN = coeffN.get(coeffN.size() - 1) / gN;
        	
        	if (!("HARD".equals(difficulty) && Math.abs(newLeadN) == 1)) {
                for (int i = 0; i < coeffN.size(); i++) {
                    coeffN.set(i,coeffN.get(i) / gN);
                }
        	}
        }
        
        if (gD > 1) {
        	
        	int newLeadD = coeffD.get(coeffD.size() - 1) / gD;
        	
        	if (!("HARD".equals(difficulty) && Math.abs(newLeadD) == 1)) {
                for (int i = 0; i < coeffD.size(); i++) {
                    coeffD.set(i,coeffD.get(i) / gD);
                }
        	}
        }
        
        Poly N = new Poly(coeffN,rootsN);
        Poly D = new Poly(coeffD,rootsD);
        
        // need at least degree 1 in denom
        if (D.degree() < 1) return null;
        
        return new RationalFunction(N,D);
    }
    
    
    public static String buildProblemText
    (RationalFunction rf) {
    	
        StringBuilder sb = new StringBuilder();
        
        sb.append("--- Rational Function ---\n");
        sb.append("f(x) = N(x) / D(x)\n");
        sb.append("N(x) = ").append(rf.numer.toStandardString()).append("\n");
        sb.append("D(x) = ").append(rf.denom.toStandardString()).append("\n\n");
        
        sb.append("--- Practice Prompts ---\n");
        sb.append("1) Fully factor f(x)\n");
        sb.append("2) Identify x- and y-intercepts.\n");
        sb.append("3) Describe end behavior.\n");
        sb.append("4) State the domain of f(x) in interval notation.\n");
        sb.append("5) Determine asymptotes.\n");
        
        return sb.toString();
    }
    
    
    public static String buildAnswerKey
    (RationalFunction rf) {
    	
        StringBuilder out = new StringBuilder();
        
        out.append("--- Answer Key ---\n");
        out.append("N(x) factored: ").append(rf.numer.toFactorString()).append("\n");
        out.append("D(x) factored: ").append(rf.denom.toFactorString()).append("\n");
        
        // x intercepts from numerator roots
        out.append("x-intercepts: ")
           .append(rf.numer.rootsWithMultiplicity()).append("\n");
        
        // y intercept at x = 0 if denom not 0
        if (!rf.denom.coeffs.isEmpty() && rf.denom.coeffs.get(0) != 0) {
        	
            double yint = (double) rf.numer.coeffs.get(0) 
                        / rf.denom.coeffs.get(0);
            
            out.append("y-intercept: (0, ").append(yint).append(")\n");
        }
        else {
            out.append("y-intercept: undefined (denominator zero at x = 0)\n");
        }
        
        // vertical asymptotes from denom roots
        out.append("Vertical asymptotes: x = ")
           .append(rf.denom.rootsWithMultiplicity()).append("\n");
        
        // domain excludes denom roots
        out.append("Domain: all real numbers except x = ")
           .append(rf.denom.rootsWithMultiplicity()).append("\n");
        
        // simple slant asymptote
        out.append("Oblique/slant asymptote: ")
           .append(getObliqueAsymptote(rf.numer,rf.denom))
           .append("\n");
        
        return out.toString();
    }
    
    
    public static String getObliqueAsymptote
    (Poly numer,Poly denom) {
    	
        // only when degN = degD + 1
        if (numer.degree() != denom.degree() + 1) return "None";
        
        int nLead = numer.coeffs.get(numer.degree());
        int dLead = denom.coeffs.get(denom.degree());
        int k = nLead / dLead; // slope
        
        return "y = " + k + "x";
    }
    
    
    // ====== internal utilities ======
    
    
    private static int randInt
    (Random rng,int lo,int hi) {
    	
        if (lo == hi) return lo;
        return lo + rng.nextInt(hi - lo + 1);
    }
    
    
    private static int randomNonZeroDifferent
    (int current,int abs,HashSet<Integer> forbidden,Random rng) {
    	
        int v = current;
        int guard = 0;
        
        // keep picking until not same and not forbidden
        while ((v == current || forbidden.contains(v)) && guard < 50) {
        	
            v = randInt(rng,-abs,abs);
            if (v == 0) v = 1; // avoid 0
            guard++;
        }
        
        return v;
    }
    
    
    private static ArrayList<Integer> randomRootsWithMultiplicity
    (int degree,int abs,boolean allowMult,Random rng) {
    	
        ArrayList<Integer> roots = new ArrayList<>();
        int remaining = degree;
        
        // keep adding roots until degree reached
        while (remaining > 0) {
        	
            int root = randInt(rng,-abs,abs);
            if (root == 0) root = 1;
            
            int mult = 1;
            
            // sometimes make a double root
            if (allowMult && remaining >= 2 && rng.nextDouble() < 0.25) {
                mult = Math.min(2,remaining);
            }
            
            for (int i = 0; i < mult; i++) {
            	roots.add(root);
            }
            
            remaining -= mult;
        }
        
        // shuffle root order for variety
        Collections.shuffle(roots,rng);
        
        return roots; // list of roots with repeats
    }

    
    private static ArrayList<Integer> expandFromRoots
    (ArrayList<Integer> roots,int leadingCoeff) {
    	
        ArrayList<Integer> poly = new ArrayList<>();
        poly.add(leadingCoeff); // start with leading coeff
        
        // repeatedly multiply by (x - r)
        for (int r : roots) {
        	
            ArrayList<Integer> next = new ArrayList<>();
            
            // new constant term
            next.add(-r * poly.get(0));
            
            // middle terms
            for (int j = 1; j < poly.size(); j++) {
                next.add(poly.get(j - 1) - r * poly.get(j));
            }
            
            // highest degree term
            next.add(poly.get(poly.size() - 1));
            
            poly = next;
        }
        
        return poly;
    }

    
    private static ArrayList<Integer> trimZeros
    (ArrayList<Integer> coeffs) {
    	
        int k = coeffs.size();
        
        // remove trailing zeros from high degree end
        while (k > 0 && coeffs.get(k - 1) == 0) {
        	k--;
        }
        
        ArrayList<Integer> out = new ArrayList<>();
        
        for (int i = 0; i < k; i++) {
        	out.add(coeffs.get(i));
        }
        
        return out;
    }
    
    
    private static int gcd
    (int a,int b) {
    	
        a = Math.abs(a);
        b = Math.abs(b);
        
        if (a == 0) return b;
        if (b == 0) return a;
        
        // euclidean algorithm
        while (b != 0) {
            int t = a % b;
            a = b;
            b = t;
        }
        
        return a;
    }

    
    private static int gcdList
    (ArrayList<Integer> list) {
    	
        int g = 0;
        
        // gcd of all numbers in list
        for (int value : list) {
            g = gcd(g,value);
        }
        
        if (g == 0) g = 1;
        
        return g;
    }
    
    
    private static int pickNonUnitLeading
    (Random rng) {
    	
    	// pick from {-3,-2,2,3}, avoid ±1 and 0
    	int v = 0;
    	
    	while (v == 0 || Math.abs(v) == 1) {
    		v = randInt(rng,-3,3);
    	}
    	
    	return v;
    }
}
