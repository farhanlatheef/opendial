// =================================================================                                                                   
// Copyright (C) 2011-2015 Pierre Lison (plison@ifi.uio.no)                                                                            
//                                                                                                                                     
// This library is free software; you can redistribute it and/or                                                                       
// modify it under the terms of the GNU Lesser General Public License                                                                  
// as published by the Free Software Foundation; either version 2.1 of                                                                 
// the License, or (at your option) any later version.                                                                                 
//                                                                                                                                     
// This library is distributed in the hope that it will be useful, but                                                                 
// WITHOUT ANY WARRANTY; without even the implied warranty of                                                                          
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU                                                                    
// Lesser General Public License for more details.                                                                                     
//                                                                                                                                     
// You should have received a copy of the GNU Lesser General Public                                                                    
// License along with this program; if not, write to the Free Software                                                                 
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA                                                                           
// 02111-1307, USA.                                                                                                                    
// =================================================================                                                                   

package opendial.domains;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import org.junit.Test;

import opendial.DialogueSystem;
import opendial.arch.Settings;
import opendial.arch.DialException;
import opendial.arch.Logger;
import opendial.bn.distribs.ProbDistribution;
import opendial.bn.distribs.discrete.CategoricalTable;
import opendial.bn.nodes.BNode;
import opendial.bn.values.DoubleVal;
import opendial.bn.values.Value;
import opendial.bn.values.ValueFactory;
import opendial.common.InferenceChecks;
import opendial.datastructs.Assignment;
import opendial.inference.InferenceAlgorithm;
import opendial.inference.exact.VariableElimination;
import opendial.inference.queries.ProbQuery;
import opendial.modules.ForwardPlanner;
import opendial.readers.XMLDomainReader;
import opendial.state.StatePruner;

/**
 * 
 *
 * @author  Pierre Lison (plison@ifi.uio.no)
 * @version $Date::                      $
 *
 */
public class BasicRuleTest {

	// logger
	public static Logger log = new Logger("BasicRuleTest", Logger.Level.DEBUG);

	public static final String domainFile = "test//domains//domain1.xml";

	static InferenceChecks inference;
	static Domain domain;

	static {
		try { 
			domain = XMLDomainReader.extractDomain(domainFile); 
			inference = new InferenceChecks();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	@Test
	public void test() throws DialException, InterruptedException {
	
		
		DialogueSystem system = new DialogueSystem(domain);
		system.detachModule(system.getModule(ForwardPlanner.class));
		StatePruner.ENABLE_PRUNING = false;
		system.getSettings().showGUI = false;
		system.startSystem(); 
	
		ProbQuery query = new ProbQuery(system.getState(),"a_u");
		inference.checkProb(query, new Assignment("a_u", "Greeting"), 0.8);
		inference.checkProb(query, new Assignment("a_u", "None"), 0.2);
	
		StatePruner.ENABLE_PRUNING = true;
	}


	@Test 
	public void test2() throws DialException {
				
		DialogueSystem system = new DialogueSystem(domain);
		system.detachModule(system.getModule(ForwardPlanner.class));
		StatePruner.ENABLE_PRUNING = false;
		system.getSettings().showGUI = false;
		system.startSystem(); 
		
		ProbQuery query = new ProbQuery(system.getState(),"i_u");
		inference.checkProb(query, new Assignment("i_u", "Inform"), 0.7*0.8);
		inference.checkProb(query, new Assignment("i_u", "None"), 1-0.7*0.8);
		
		StatePruner.ENABLE_PRUNING = true;
	}

	@Test
	public void test3() throws DialException {

		inference.EXACT_THRESHOLD = 0.06;
		
		DialogueSystem system = new DialogueSystem(domain);
		system.detachModule(system.getModule(ForwardPlanner.class));
		StatePruner.ENABLE_PRUNING = false;
		system.getSettings().showGUI = false;
		system.startSystem(); 
		ProbQuery query = new ProbQuery(system.getState(),"direction");
		inference.checkProb(query, new Assignment("direction", "straight"), 0.79);
		inference.checkProb(query, new Assignment("direction", "left"), 0.20);
		inference.checkProb(query, new Assignment("direction", "right"), 0.01);
		
		StatePruner.ENABLE_PRUNING = true;
	}


	@Test
	public void test4() throws DialException {
		
		DialogueSystem system = new DialogueSystem(domain);
		system.detachModule(system.getModule(ForwardPlanner.class));
		StatePruner.ENABLE_PRUNING = false;
		system.getSettings().showGUI = false;
		system.startSystem(); 
		
		ProbQuery query = new ProbQuery(system.getState(),"o");

		inference.checkProb(query, new Assignment("o", "and we have var1=value2"), 0.3);
		inference.checkProb(query, new Assignment("o", "and we have localvar=value1"), 0.35);
		inference.checkProb(query, new Assignment("o", "and we have localvar=value3"), 0.31);

		StatePruner.ENABLE_PRUNING = true;
	}


	@Test
	public void test5() throws DialException {
		
		DialogueSystem system = new DialogueSystem(domain);
		system.detachModule(system.getModule(ForwardPlanner.class));
		StatePruner.ENABLE_PRUNING = false;
		system.getSettings().showGUI = false;
		system.startSystem(); 
		
		ProbQuery query = new ProbQuery(system.getState(),"o2");
	
		inference.checkProb(query, new Assignment("o2", "here is value1"), 0.35);
		inference.checkProb(query, new Assignment("o2", "and value2 is over there"), 0.07);
		inference.checkProb(query, new Assignment("o2", "value3, finally"), 0.28);
		
		StatePruner.ENABLE_PRUNING = true;

	}

	@Test
	public void test6() throws DialException, InterruptedException {

		inference.EXACT_THRESHOLD = 0.06;

		DialogueSystem system = new DialogueSystem(domain);
		system.detachModule(system.getModule(ForwardPlanner.class));
		system.getSettings().showGUI = false;
		system.startSystem(); 
		CategoricalTable table = new CategoricalTable();
		table.addRow(new Assignment("var1", "value2"), 0.9);
		system.addContent(table);
		
		ProbQuery query = new ProbQuery(system.getState(),"o");

		inference.checkProb(query, new Assignment("o", "and we have var1=value2"), 0.9);
		inference.checkProb(query, new Assignment("o", "and we have localvar=value1"), 0.05);
		inference.checkProb(query, new Assignment("o", "and we have localvar=value3"), 0.04);
		
		StatePruner.ENABLE_PRUNING = true;
	}


	@Test
	public void test7() throws DialException, InterruptedException {

		DialogueSystem system = new DialogueSystem(domain);
		system.detachModule(system.getModule(ForwardPlanner.class));
		StatePruner.ENABLE_PRUNING = false;
		system.getSettings().showGUI = false;
		system.startSystem(); 
		
		ProbQuery query = new ProbQuery(system.getState(),"a_u2");
		inference.checkProb(query, new Assignment("a_u2", "[HowAreYou, Greet]"), 0.7);
		inference.checkProb(query, new Assignment("a_u2", "none"), 0.1);
		inference.checkProb(query, new Assignment("a_u2", "[HowAreYou]"), 0.2);
		
		StatePruner.ENABLE_PRUNING = true;
	}

	
	// OUTDATED
//	@Test
	public void test8() throws DialException, InterruptedException {

		DialogueSystem system = new DialogueSystem(domain);
		system.detachModule(system.getModule(ForwardPlanner.class));
		StatePruner.ENABLE_PRUNING = false;
		system.getSettings().showGUI = false;
		system.startSystem(); 
		ProbQuery query = new ProbQuery(system.getState(),"a_u3");

		SortedSet<String> createdNodes = new TreeSet<String>();
		for (String nodeId: system.getState().getNodeIds()) {
			if (nodeId.contains("a_u3^")) {
				createdNodes.add(nodeId);
			}
		}
		
		assertEquals(2, createdNodes.size());

		String greetNode = "";
		String howareyouNode = "";
		Set<Value> values = system.getState().getNode(createdNodes.first()+"").getValues();
		if (values.contains(ValueFactory.create("Greet"))) {
			greetNode = createdNodes.first();
			howareyouNode = createdNodes.last();
		}
		else {
			greetNode = createdNodes.last();
			howareyouNode = createdNodes.first();
		}
		
		
		ProbQuery query2 = new ProbQuery(system.getState(),greetNode+".start");
		ProbQuery query3 = new ProbQuery(system.getState(),greetNode+".end");
		ProbQuery query4 = new ProbQuery(system.getState(),howareyouNode+".start");
		ProbQuery query5 = new ProbQuery(system.getState(),howareyouNode+".end");
		ProbQuery query6 = new ProbQuery(system.getState(),greetNode+"");
		ProbQuery query7 = new ProbQuery(system.getState(),howareyouNode+"");

		inference.checkProb(query, new Assignment("a_u3", "["+greetNode +"," + howareyouNode +"]"), 0.7);
		inference.checkProb(query, new Assignment("a_u3", "none"), 0.1);
		inference.checkProb(query, new Assignment("a_u3", "[" + howareyouNode + "]"), 0.2);
		inference.checkProb(query2, new Assignment(greetNode+".start", 0), 0.7);
		inference.checkProb(query3, new Assignment(greetNode+".end", 5), 0.7);
		inference.checkProb(query4, new Assignment(howareyouNode+".start", 12), 0.7);
		inference.checkProb(query5, new Assignment(howareyouNode+".end", 23), 0.7);
		inference.checkProb(query5, new Assignment(howareyouNode+".end", 22), 0.2);
		inference.checkProb(query6,  new Assignment(greetNode+"", "Greet"), 0.7);
		inference.checkProb(query7,  new Assignment(howareyouNode+"", "HowAreYou"), 0.9); 
			
		StatePruner.ENABLE_PRUNING = true;
	}
	
	
}
