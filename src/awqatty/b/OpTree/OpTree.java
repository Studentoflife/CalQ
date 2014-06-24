package awqatty.b.OpTree;

import awqatty.b.CustomExceptions.BranchCountException;
import awqatty.b.CustomExceptions.CalculationException;
import awqatty.b.FunctionDictionary.FunctionType;
import awqatty.b.ListTree.ListTree;
import awqatty.b.MathmlPresentation.TagFlags;

public class OpTree {

	// Private Members
	private ListTree<OpNode> tree = new ListTree<OpNode>();
	private OpNodeBuilder node_builder = new OpNodeBuilder();
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
		*/
	}
	private void setHighlight() {
		/* TODO uncomment method
		if (selection != 0)		// highlighting everything == highlighting nothing
			tree.get(selection).enableTagFlag(TagFlags.HIGHLIGHT); 
		//*/
	}
	
	private void setParentheses(int index, FunctionType parent_type) {
		if (parent_type.doesEncapsulateBranches() || tree.get(index).ftype.isEncapsulated())
			tree.get(index).disableTagFlag(TagFlags.PARENTHESES);
		else
			tree.get(index).enableTagFlag(TagFlags.PARENTHESES);			
	}
	private void setParentheses(int index) {
		int tmp = tree.getParentIndex(index);
		if (tmp != -1)
			setParentheses(index, tree.get(tmp).ftype);
		else
			tree.get(index).disableTagFlag(TagFlags.PARENTHESES);
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
							tree.get(selection).getBranchCount(),
							node_builder.build(FunctionType.BLANK) );
					setHighlight();
					// Set ID numbers for all elements in new locations
					for (; i<tree.size(); ++i)
						tree.get(i).setIdNumber(i);
					return;
				}
		}
		unsetHighlight();
		// Set new function elements into place
		tree.addParent(selection,
				node_builder.buildExpLoc(ftype),
				node_builder.buildExp(ftype) );
		
		// Set ID numbers for all elements in new locations
		for (i=selection; i<tree.size(); ++i)
			tree.get(i).setIdNumber(i);
		
		setParentheses(selection);
		// Set index to new location, & set parentheses
		int[] indices = tree.getBranchIndices(selection);
		for (i=0; i < tree.get(selection).getBranchCount(); ++i) {
			if (tree.get(indices[i]).ftype == FunctionType.BLANK) {
				selection = indices[i];
			}
			setParentheses(indices[i], ftype);
		}
		setHighlight();
	}
	
	/*****************************************************************
	 * 
	 */
	public void addNumber(double num) {
		boolean has_branch = tree.get(selection).getBranchCount() > 0;
		tree.setBranch(selection, 
				node_builder.Number(num)
							.build(FunctionType.NUMBER) );
		
		setHighlight();
		setParentheses(selection);
		// Set ID numbers for all elements in new locations
		if (has_branch)
			for (int i=selection; i<tree.size(); ++i)
				tree.get(i).setIdNumber(i);
		else
			tree.get(selection).setIdNumber(selection);
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
			
			setParentheses(selection);
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
				setParentheses(selection);
				// Set ID numbers for all elements in new locations
				for (i=selection; i<tree.size(); ++i)
					tree.get(i).setIdNumber(i);
			}
		}
	}
	
	//-----------------------------------------------------------------
	// Calculation Methods
	public double getCalculation() throws CalculationException {
		return tree.calculate(new BranchCompute()).get(0);
	}
	public String getTextPres() {
		return tree.calculate(new BranchDisplay()).get(0);
	}

}
