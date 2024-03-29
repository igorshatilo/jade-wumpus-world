package wumpusworld.core.logic.propositional.transformations;

import wumpusworld.core.logic.propositional.parsing.AbstractPLVisitor;
import wumpusworld.core.logic.propositional.parsing.ast.ComplexSentence;
import wumpusworld.core.logic.propositional.parsing.ast.Connective;
import wumpusworld.core.logic.propositional.parsing.ast.Sentence;

/**
 * Artificial Intelligence A Modern Approach (3rd Edition): page 249.<br>
 * <br>
 * Distributivity of & over |:<br>
 * (&alpha; & (&beta; | &gamma;))<br>
 * &equiv;<br>
 * ((&alpha; & &beta;) | (&alpha; & &gamma;))<br>
 *
 * @author Ciaran O'Reilly
 * @author Ruediger Lunde
 */
public class DistributeAndOverOr extends AbstractPLVisitor<Object> {

	/**
	 * Distribute and (&) over or (|).
	 *
	 * @param sentence a propositional logic sentence. This sentence should already
	 * have biconditionals, and implications removed and negations
	 * moved inwards.
	 * @return an equivalent Sentence to the input with and's distributed over
	 * or's.
	 */
	public static Sentence apply(Sentence sentence) {
		return sentence.accept(new DistributeAndOverOr(), null);
	}

	@Override
	public Sentence visitBinarySentence(ComplexSentence s, Object arg) {
		Sentence result;

		if(s.isAndSentence()) {
			Sentence s1 = s.getSimplerSentence(0).accept(this, arg);
			Sentence s2 = s.getSimplerSentence(1).accept(this, arg);
			if(s1.isOrSentence() || s2.isOrSentence()) {
				Sentence alpha, betaAndGamma;
				if(s2.isOrSentence()) {
					// (&alpha; & (&beta; | &gamma;))
					// Note: even if both are 'or' sentence
					// we will prefer to use s2
					alpha = s1;
					betaAndGamma = s2;
				} else {
					// Note: Need to handle this case too
					// ((&beta; | &gamma;) & &alpha;)
					alpha = s2;
					betaAndGamma = s1;
				}

				Sentence beta = betaAndGamma.getSimplerSentence(0);
				Sentence gamma = betaAndGamma.getSimplerSentence(1);

				if(s2.isOrSentence()) {
					// ((&alpha; & &beta;) | (&alpha; & &gamma;))
					Sentence alphaAndBeta = (new ComplexSentence(Connective.AND,
							alpha, beta)).accept(this, null);
					Sentence alphaAndGamma = (new ComplexSentence(Connective.AND,
							alpha, gamma)).accept(this, null);

					result = new ComplexSentence(Connective.OR, alphaAndBeta,
							alphaAndGamma);
				} else {
					// ((&beta; & &alpha;) | (&gamma; & &alpha;))
					Sentence betaAndAlpha = (new ComplexSentence(Connective.AND,
							beta, alpha)).accept(this, null);
					Sentence gammaAndAlpha = (new ComplexSentence(Connective.AND,
							gamma, alpha)).accept(this, null);

					result = new ComplexSentence(Connective.OR, betaAndAlpha,
							gammaAndAlpha);
				}
			} else {
				result = new ComplexSentence(Connective.AND, s1, s2);
			}
		} else {
			result = super.visitBinarySentence(s, arg);
		}

		return result;
	}
}
