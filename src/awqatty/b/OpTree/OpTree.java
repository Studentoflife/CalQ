 package awqatty.b.OpTree;

import awqatty.b.CustomExceptions.BranchCountException;
import awqatty.b.CustomExceptions.CalculationException;
import awqatty.b.FunctionDictionary.FunctionType;
import awqatty.b.ListTree.ListTree;
import awqatty.b.MathmlPresentation.TagFlags;
import awqatty.b.MathmlPresentation.Tags;

public class OpTree {

	// Private Members
	private final ListTree<OpNode> tree = new ListTree<OpNode>();
	private final OpNodeBuilder node_builder = new OpNodeBuilder();
	public int selection = 0;
	
	// Constructor
	public OpTree() {
		tree.addChild(-1, 0, node_builder.build(FunctionType.BLANK));
		tree.get(0).setIdNumber(0);
	}
	
	//---------------------------------------------------------------
	// Private Methods
	private void unsetHighlight() {
		/* TODO uncomment method
		tree.get(selection).disableTagFlag(TagFlags.HIGHLIGHT);
		//*/
	}
	private void setHighlight() {
		/* TODO uncomment method
		if (selection != 0)		// highlighting everything == highlighting nothing
			tree.get(selection).enableTagFlag(TagFlags.HIGHLIGHT); 
		//*/
	}
		
	//---------------------------------------------------------------
	// Manipulation Methods
	public void setSelection(int index) {
		unsetHighlight();
		selection=index;
		setHighlight();
	}
	
	/****************************************************************
	 * 
	 */
	public void addFunction(FunctionType ftype) {
		int i;
		// Condition for commutative operations
		if (ftype.isCommutative()) {
			if (ftype == tree.get(selection).ftype) {
				i = selection = tree.addChild(selection,
						tree.get(selection).getBranchCount(),
						node_builder.build(FunctionType.BLANK) );
				// Set ID numbers for all elements in new locations
				for (; i<tree.size(); ++i)
					tree.get(i).setIdNumber(i);
				return;
			}
			else if (selection != 0)
				if (ftype == tree.get(tree.getParentIndex(selection)).ftype) {
					unsetHighlight();
					i = selection = tree.addChild(tree.getParentIndex(selection),
							tree.getBranchNumber(selection)+1,
							node_builder.build(FunctionType.BLANK) );
					setHighlight();
					// Set ID numbers for all elements in new locations
					for (; i<tree.size(); ++i)
						tree.get(i).setIdNumber(i);
					return;
				}
		}
		unsetHighlight();
		i = selection;
		// Set new function elements into place
		node_builder.buildInSubtree(tree.subTree(selection), ftype);
		
		int last_index = tree.size();
		// Set ID numbers for all elements in new locations
		for (; i<last_index; ++i)
			tree.get(i).setIdNumber(i);
		
		// Set index to new location
		int[] indices = tree.getBranchIndices(selection);
		last_index = tree.get(selection).getBranchCount();
		for (i=0; i<last_index; ++i) {
			if (tree.get(indices[i]).ftype == FunctionType.BLANK) {
				selection = indices[i];
				break;
			}
		}
		setHighlight();
	}
	
	/*****************************************************************
	 * 
	 */
	public void addNumber(double num) {
		boolean has_branch = tree.get(selection).getBranchCount() > 0;
		node_builder.Number(num)
					.buildInSubtree(tree.subTree(selection), FunctionType.NUMBER);
		
		// Set ID numbers for all elements in new locations
		if (has_branch)
			for (int i=selection; i<tree.size(); ++i)
				tree.get(i).setIdNumber(i);
		else
			tree.get(selection).setIdNumber(selection);
		setHighlight();
	}
	
	public void shuffleOrder() {
		ListTree<OpNode>.FindParentAlgorithm alg = tree.new FindParentAlgorithm();
		alg.runAlgorithm(selection);
		
		int parent_loc = alg.getParentIndex();
		int i = selection;
		selection = tree.shiftBranchOrder(selection,
				(alg.getBranchNumber()+1) % tree.get(parent_loc).getBranchCount() );
		
		int last_loc = tree.getEndOfBranchIndex(Math.max(i,selection));
		// Set ID numbers for all elements in new locations
		for (i = Math.min(i,selection); i < last_loc; ++i)
			tree.get(i).setIdNumber(i);
		// No need to reset highlight, selection shifts w/ object movement
	}
	
	/*****************************************************************
	 * 
	 */
	public void delete() {
		if (tree.get(selection).ftype != FunctionType.BLANK) {
			// Non-blank branches are replaced with a blank node
			boolean has_branch = tree.get(selection).getBranchCount() > 0;
			tree.setBranch(selection, 
					node_builder.build(FunctionType.BLANK) );
			
			setHighlight();
			// Set ID numbers for all elements in new locations
			if (has_branch)
				for (int i=selection; i<tree.size(); ++i)
					tree.get(i).setIdNumber(i);
			else
				tree.get(selection).setIdNumber(selection);
		}
		else if (selection != 0) {
		/* Deletes blank node from parent's branch list
		 * if this puts parent below min. branch count,
		 * 		delete parent, leave non-blank branch in its place
		 */
			int i;
			try {
				i = selection;
				selection = tree.getParentIndex(selection);
				tree.deleteBranch(i);
				
				setHighlight();
				// Set ID numbers for all elements in new locations
				for (i=selection; i<tree.size(); ++i)
					tree.get(i).setIdNumber(i);
			}
			catch (BranchCountException bce) {
				// Replace parent with last non-BLANK branch of parent
				int[] indices = tree.getBranchIndices(selection);
				for (i=indices.length-1; i > 0 ; --i) {
					if (tree.get(indices[i]).ftype != FunctionType.BLANK) {
						break;
					}
				}
				tree.setBranch(selection,
						new ListTree<OpNode>(tree.subTree(indices[i])) );
				
				setHighlight();
				// Set ID numbers for all elements in new locations
				for (i=selection; i<tree.size(); ++i)
					tree.get(i).setIdNumber(i);
			}
		}
	}
	
	//-----------------------------------------------------------------
	// Calculation Methods
	public double getCalculation() throws CalculationException {
		return new BranchCalculator().runLoop(tree).get(0);
	}
	public String getTextPres() {
		return new BranchXmlDisplay((byte)0).runLoop(tree).get(0)
				.replaceAll(Tags.PARENTHESIS_L.getTag(), "")
				.replaceAll(Tags.PARENTHESIS_R.getTag(), "");
	}

}
