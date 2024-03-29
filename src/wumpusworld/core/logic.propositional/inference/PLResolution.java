package wumpusworld.core.logic.propositional.inference;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import wumpusworld.core.logic.propositional.kb.KnowledgeBase;
import wumpusworld.core.logic.propositional.kb.data.Clause;
import wumpusworld.core.logic.propositional.kb.data.Literal;
import wumpusworld.core.logic.propositional.parsing.ast.ComplexSentence;
import wumpusworld.core.logic.propositional.parsing.ast.Connective;
import wumpusworld.core.logic.propositional.parsing.ast.PropositionSymbol;
import wumpusworld.core.logic.propositional.parsing.ast.Sentence;
import wumpusworld.core.logic.propositional.transformations.ConvertToConjunctionOfClauses;
import wumpusworld.core.util.SetOps;

/**
 * Artificial Intelligence A Modern Approach (3rd Edition): page 255.<br>
 * <br>
 *
 * <pre>
 * <code>
 * function PL-RESOLUTION(KB, &alpha;) returns true or false
 *    inputs: KB, the knowledge base, a sentence in propositional logic
 *            &alpha;, the query, a sentence in propositional logic
 *
 *    clauses &larr; the set of clauses in the CNF representation of KB &and; &not;&alpha;
 *    new &larr; {}
 *    loop do
 *       for each pair of clauses C<sub>i</sub>, C<sub>j</sub> in clauses do
 *          resolvents &larr; PL-RESOLVE(C<sub>i</sub>, C<sub>j</sub>)
 *          if resolvents contains the empty clause then return true
 *          new &larr; new &cup; resolvents
 *       if new &sube; clauses then return false
 *       clauses &larr; clauses &cup; new
 * </code>
 * </pre>
 * <p>
 * Figure 7.12 A simple resolution algorithm for propositional logic. The
 * function PL-RESOLVE returns the set of all possible clauses obtained by
 * resolving its two inputs.<br>
 * <br>
 * Note: Optional optimization added to implementation whereby tautological
 * clauses can be removed during processing of the algorithm - see pg. 254 of
 * AIMA3e:<br>
 * <blockquote> Inspection of Figure 7.13 reveals that many resolution steps are
 * pointless. For example, the clause B<sub>1,1</sub> &or; &not;B<sub>1,1</sub>
 * &or; P<sub>1,2</sub> is equivalent to <i>True</i> &or; P<sub>1,2</sub> which
 * is equivalent to <i>True</i>. Deducing that <i>True</i> is true is not very
 * helpful. Therefore, any clauses in which two complementary literals appear
 * can be discarded. </blockquote>
 *
 * @author Ciaran O'Reilly
 * @author Ravi Mohan
 * @author Mike Stampone
 * @author Ruediger Lunde
 * @see Clause#isTautology()
 */
public class PLResolution implements EntailmentChecker {
	private boolean discardTautologies = true;

	/**
	 * Default constructor, which will set the algorithm to discard tautologies
	 * by default.
	 */
	public PLResolution() {
		this(true);
	}

	//
	// SUPPORTING CODE
	//

	/**
	 * Constructor.
	 *
	 * @param discardTautologies true if the algorithm is to discard tautological clauses
	 * during processing, false otherwise.
	 */
	public PLResolution(boolean discardTautologies) {
		setDiscardTautologies(discardTautologies);
	}

	/**
	 * PL-RESOLUTION(KB, &alpha;)<br>
	 * A simple resolution algorithm for propositional logic.
	 *
	 * @param kb the knowledge base, a sentence in propositional logic.
	 * @param alpha the query, a sentence in propositional logic.
	 * @return true if KB |= &alpha;, false otherwise.
	 */
	public boolean isEntailed(KnowledgeBase kb, Sentence alpha) {
		// clauses <- the set of clauses in the CNF representation
		// of KB & ~alpha
		Set<Clause> clauses = convertKBAndNotAlphaIntoCNF(kb, alpha);
		// new <- {}
		Set<Clause> newClauses = new LinkedHashSet<>();
		// loop do
		while(true) {
			// for each pair of clauses C_i, C_j in clauses do
			List<Clause> clausesList = new ArrayList<>(clauses);
			for(int i = 0; i < clausesList.size() - 1; i++) {
				Clause ci = clausesList.get(i);
				for(int j = i + 1; j < clausesList.size(); j++) {
					Clause cj = clausesList.get(j);
					// resolvents <- PL-RESOLVE(C_i, C_j)
					Set<Clause> resolvents = plResolve(ci, cj);
					// if resolvents contains the empty clause then return true
					if(resolvents.contains(Clause.EMPTY)) {
						return true;
					}
					// new <- new U resolvents
					newClauses.addAll(resolvents);
				}
			}
			// if new is subset of clauses then return false
			if(clauses.containsAll(newClauses)) {
				return false;
			}

			// clauses <- clauses U new
			clauses.addAll(newClauses);
		}
	}

	/**
	 * PL-RESOLVE(C<sub>i</sub>, C<sub>j</sub>)<br>
	 * Calculate the set of all possible clauses by resolving its two inputs.
	 *
	 * @param ci clause 1
	 * @param cj clause 2
	 * @return the set of all possible clauses obtained by resolving its two
	 * inputs.
	 */
	public Set<Clause> plResolve(Clause ci, Clause cj) {
		Set<Clause> resolvents = new LinkedHashSet<>();

		// The complementary positive literals from C_i
		resolvePositiveWithNegative(ci, cj, resolvents);
		// The complementary negative literals from C_i
		resolvePositiveWithNegative(cj, ci, resolvents);

		return resolvents;
	}

	/**
	 * @return true if the algorithm will discard tautological clauses during
	 * processing.
	 */
	public boolean isDiscardTautologies() {
		return discardTautologies;
	}

	/**
	 * Determine whether or not the algorithm should discard tautological
	 * clauses during processing.
	 */
	public void setDiscardTautologies(boolean discardTautologies) {
		this.discardTautologies = discardTautologies;
	}


	protected Set<Clause> convertKBAndNotAlphaIntoCNF(KnowledgeBase kb, Sentence alpha) {

		// KB & ~alpha;
		Sentence isContradiction = new ComplexSentence(Connective.AND,
				kb.asSentence(), new ComplexSentence(Connective.NOT, alpha));
		// the set of clauses in the CNF representation
		Set<Clause> clauses = new LinkedHashSet<>(
				ConvertToConjunctionOfClauses.apply(isContradiction)
						.getClauses());

		discardTautologies(clauses);

		return clauses;
	}

	protected void resolvePositiveWithNegative(Clause c1, Clause c2,
	                                           Set<Clause> resolvents) {
		// Calculate the complementary positive literals from c1 with
		// the negative literals from c2
		Set<PropositionSymbol> complementary = SetOps.intersection(
				c1.getPositiveSymbols(), c2.getNegativeSymbols());
		// Construct a resolvent clause for each complement found
		for(PropositionSymbol complement : complementary) {
			List<Literal> resolventLiterals = new ArrayList<>();
			// Retrieve the literals from c1 that are not the complement
			for(Literal c1l : c1.getLiterals()) {
				if(c1l.isNegativeLiteral() || !c1l.getAtomicSentence().equals(complement)) {
					resolventLiterals.add(c1l);
				}
			}
			// Retrieve the literals from c2 that are not the complement
			for(Literal c2l : c2.getLiterals()) {
				if(c2l.isPositiveLiteral() || !c2l.getAtomicSentence().equals(complement)) {
					resolventLiterals.add(c2l);
				}
			}
			// Construct the resolvent clause
			Clause resolvent = new Clause(resolventLiterals);
			// Discard tautological clauses if this optimization is turned on.
			if(!(isDiscardTautologies() && resolvent.isTautology())) {
				resolvents.add(resolvent);
			}
		}
	}

	// Utility routine for removing the tautological clauses from a set (in place).
	protected void discardTautologies(Set<Clause> clauses) {
		if(isDiscardTautologies())
			clauses.removeIf(Clause::isTautology);
	}
}