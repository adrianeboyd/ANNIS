/*
 * Copyright 2013 SFB 632.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

parser grammar AqlParser;

options
{
  language=Java;
  tokenVocab=AqlLexer;
}


start 
	: exprTop EOF
	;

textSpec 
	:	START_TEXT_PLAIN  END_TEXT_PLAIN # EmptyExactTextSpec
  | START_TEXT_PLAIN content=TEXT_PLAIN END_TEXT_PLAIN # ExactTextSpec
  | START_TEXT_REGEX END_TEXT_REGEX #EmptyRegexTextSpec
  | START_TEXT_REGEX content=TEXT_REGEX END_TEXT_REGEX # RegexTextSpec
	;

rangeSpec
  : min=DIGITS (COMMA max=DIGITS)?
  ;

qName
	:	namespace=ID COLON name=ID
	|	name=ID
	;

edgeAnno
	:	 name=qName op=(EQ|NEQ) value=textSpec
	;

edgeSpec
	: BRACKET_OPEN edgeAnno+ BRACKET_CLOSE
	;

refOrNode
  : REF # ReferenceRef
  | VAR_DEF? variableExpr # ReferenceNode
  ;

precedence
	: PRECEDENCE (layer=ID)? # DirectPrecedence
	| PRECEDENCE (layer=ID)? STAR # IndirectPrecedence
	| PRECEDENCE (layer=ID COMMA?)? rangeSpec #RangePrecedence
	;

near
	: NEAR (layer=ID)? # DirectNear
	| NEAR (layer=ID)? STAR # IndirectNear
	| NEAR (layer=ID COMMA?)? rangeSpec #RangeNear
	;

dominance
	: DOMINANCE (layer=ID)?  (LEFT_CHILD | RIGHT_CHILD)? (anno=edgeSpec)? # DirectDominance
	| DOMINANCE (layer=ID)? STAR # IndirectDominance
	| DOMINANCE (layer=ID)? rangeSpec # RangeDominance
	;
	
pointing
	: POINTING label=ID (anno=edgeSpec)? # DirectPointing
	| POINTING label=ID (anno=edgeSpec)? STAR # IndirectPointing
	| POINTING label=ID (anno=edgeSpec)? COMMA? rangeSpec # RangePointing
	;

spanrelation
  : IDENT_COV # IdenticalCoverage
	|	LEFT_ALIGN # LeftAlign
	|	RIGHT_ALIGN # RightAlign
	|	INCLUSION # Inclusion
	|	OVERLAP # Overlap
	|	RIGHT_OVERLAP # RightOverlap
	| LEFT_OVERLAP # LeftOverlap
; 

commonparent
  : COMMON_PARENT (label=ID)?
  ;

commonancestor
  : COMMON_PARENT (label=ID)? STAR
  ;

identity
  : IDENTITY 
  ;

equalvalue
  : EQ_VAL 
  ;

notequalvalue
  : NEQ 
  ;

operator
  : precedence
  | near
  | spanrelation
  | dominance
  | pointing
  | commonparent
  | commonancestor
  | identity
  | equalvalue
  | notequalvalue
  ;


n_ary_linguistic_term
  :  refOrNode (operator refOrNode)+ # Relation
  ;

	
unary_linguistic_term
	:	left=REF ROOT # RootTerm
	|	left=REF ARITY EQ rangeSpec # ArityTerm
	|	left=REF TOKEN_ARITY EQ rangeSpec # TokenArityTerm
	;

variableExpr
  : qName op=(EQ|NEQ) txt=textSpec # AnnoEqTextExpr
  | TOK # TokOnlyExpr 
  | NODE # NodeExpr
  | TOK op=(EQ|NEQ) txt=textSpec # TokTextExpr
	|	txt=textSpec # TextOnly // shortcut for tok="..."
  | qName # AnnoOnlyExpr
  ;

expr
  : VAR_DEF variableExpr # NamedVariableTermExpr
  | variableExpr # VariableTermExpr
	|	unary_linguistic_term # UnaryTermExpr
	|	n_ary_linguistic_term #  BinaryTermExpr
  | META DOUBLECOLON id=qName op=(EQ|NEQ) txt=textSpec # MetaTermExpr 
  ;

andTopExpr
  : ((expr (AND expr)*) | (BRACE_OPEN expr (AND expr)* BRACE_CLOSE)) # AndExpr
  ;


exprTop
  : andTopExpr (OR andTopExpr)* # OrTop
	;
