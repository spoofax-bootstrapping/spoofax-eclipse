[
   PPTable                                -- R [ H [KW["pp-table"]] KW[":"] _1],
   PrettyPrint                            -- R [ H [KW["pretty-print"]] KW[":"] _1],
   Refactorings                           -- V is=2 vs=1 [H hs=0 [KW["refactorings"] _1]  _2],
   Refactoring                            -- V is=2 [ H [ KW["refactoring"] _1  KW[":"] _2  KW["="] _3 _4] _5],
   Refactoring.1:iter-star                -- _1,
   Refactoring.4:iter-star                -- _1,
   Refactoring.5:iter                     -- _1,
   Shortcut                               -- H [KW["shortcut"] KW[":"] _1],
   InteractionId                          -- _1,
   KeyCombination                         -- _1,
   KeyCombination.1:iter-star-sep         -- _1 KW[" + "],
   KeyBinding                             -- R [ H [                   _1 ] KW[":"] H [ _2 ] ],
   KeyBindings                            -- V is=2 vs=1 [H hs=0 [KW["keybindings"] _1]  _2],
   UserInput                              -- V is=2 [H  [KW["input"]] _1],
   UserInput.1:iter                       -- _1,
   IdInputField                           -- H [KW["identifier"] KW[":"] _1],
   IdInputField                           -- H [KW["identifier"] KW[":"] _1 KW["="] _2],
   TextInputField                         -- H [KW["text"] KW[":"] _1 KW["="] _2],
   BooleanInputField                      -- H [KW["boolean"] KW[":"] _1 KW["="] _2],
   TrueValue                              -- H [KW["true"]],
   FalseValue                             -- H [KW["false"]],
   ReferenceHoverRule   -- R [ H [ KW["reference"] _1 ] KW[":"] H [ _2 _3 ] ],
   ReferenceRule        -- R [ H [ KW["reference"] _1 ] KW[":"] _2 ],
   CompletionProposer   -- R [ H [ KW["completion"] KW["proposer"] ] KW[":"] _1 ],
   CompletionLexical    -- R [ H [ KW["completion"] KW["lexical"] ] KW[":"] _1 ],
   IdentifierLexical    -- R [ H [ KW["identifier"] KW["lexical"] ] KW[":"] _1 ],
   Placeholder          -- H hs=0 [ _1 ],
   PlaceholderWithSort  -- H hs=0 [ _1 _2 KW[">"] ],
   CompletionKeyword    -- R [ H [ KW["completion"] KW["keyword"] ] KW[":"] H [ _1 _2 ] ],
   CompletionTemplate   -- R [ H [ KW["completion"] KW["template"] ] KW[":"] H [ _1 _2 _3 ] ],
   CompletionTemplate.2:iter-star -- _1,
   CompletionTemplateWithSort   -- R [ V is=2 [ H [ KW["completion"] KW["template"] KW[":"] _1 KW["="]] ]
                                                H [ _2 _3 _4 ] ],
   CompletionTemplateWithSort.3:iter-star -- _1,
   CompletionTemplateEx -- R [ V is=2 [ H [ KW["completion"] KW["template"] _1 KW[":"] _2 ]
                                        H [ _3 _4 ] ] ],
   CompletionTemplateEx.1:iter-star       -- _1,
   CompletionTemplateEx.3:iter            -- _1,
   CompletionTemplateEx.4:iter-star       -- _1,
   NoCompletionPrefix                     -- ,
   CompletionPrefix                       -- _1 KW["="],
   CompletionTrigger    -- R [ H [ KW["completion"] KW["trigger"] ] KW[":"] H [ _1 _2 ] ],
   HoverRule            -- R [ H [ KW["hover"]     _1 ] KW[":"] _2 ],
   OccurrenceRule       -- R [ H [ KW["occurrence"] _1 ] H [ KW[":"] _2 ] ],
   SemanticProvider     -- R [ H [ KW["provider"] ] KW[":"] _1 ],
   OnSave               -- R [ H [ KW["on"] KW["save"] ] KW[":"] _1 _2 ],
   Disambiguator        -- R [ H [ KW["disambiguator"] ] KW[":"] _1 ],
   SemanticObserver     -- R [ H [ KW["observer"] ] KW[":"] _1 _2 ],
   SemanticObserver.2:iter-star -- _1,
   Builder              -- R [ H [ KW["builder"] ]   KW[":"] _1 H [ KW["="] _2 _3 ] ],
   Builder.3:iter-star  -- _1,
   BuilderCaption       -- R [ H [ KW["builder"] KW["caption"] ] KW[":"] _1 ],
   MultiFile            -- KW["(multifile)"],
   OpenEditor           -- KW["(openeditor)"],
   RealTime             -- KW["(realtime)"],
   Persistent           -- KW["(persistent)"],
   Meta                 -- KW["(meta)"],
   Source               -- KW["(source)"],
   Cursor               -- KW["(cursor)"],
   Strategy             -- _1,
   Attribute            -- H hs=0 [ KW["id"] KW["."] _1 ],
   Analysis             -- V is=2 [H hs=0 [ KW["analysis"] _1 ] A(l,l,l) [_2] ],
   References           -- V is=2 [H hs=0 [ KW["references"] _1 ] A(l,l,l) [_2] ],
   Completions          -- V is=2 [H hs=0 [ KW["completions"] _1 ] A(l,l,l) [_2] ],
   Occurrences          -- V is=2 [H hs=0 [ KW["occurrences"] _1 ] A(l,l,l) [_2] ],
   Strategy             -- _1,
   Attribute            -- H hs=0 [ KW["id"] KW["."] _1 ],
   Builders             -- V is=2 [H hs=0 [KW["builders"] _1] A(l,l,l,l,l,l) [_2]],
   Builders.2:iter-star -- _1,
   Colorer              -- V is=2 [H hs=0 [KW["colorer"] _1] A(l,l,l,l,l,l) [_2]],
   Colorer.2:iter-star  -- _1,
   ColorDef             -- R [ _1 KW["="] _2],
   ColorRuleAll         -- R [ H [ KW["environment"] _1 ] KW[":"] H [ _2 ] ],
   ColorRule            -- R [ H [                   _1 ] KW[":"] H [ _2 ] ],
   ColorRuleAllNamed    -- R [ H [ KW["environment"] _1 ] KW[":"] H [ _2 ] KW["="] H [ _3 ] ],
   ColorRuleNamed       -- R [ H [                   _1 ] KW[":"] H [ _2 ] KW["="] H [ _3 ] ],
   Attribute            -- _1 _2 _3,
   Attribute            -- _1 _2 _3,
   AttributeRef         -- _1,
   FoldRuleAll          -- H [ KW["all"] _1 _2 ],
   FoldRule             -- H [ _1 _2 ],
   Blank                -- KW["(blank)"],
   Disable              -- KW["(disabled)"],
   Folded               -- KW["(folded)"],
   None                 -- ,
   OutlineRule          -- _1,
   Values                 -- H [ _1 ],
   Values.1:iter-star-sep -- _1 KW[","],
   Language             -- V is=2 [H hs=0 [KW["language"] _1] A(l,l,l) [_2]],
   Language.2:iter-star -- _1,
   LanguageName         -- R [ KW["name"] KW[":"] _1 ],
   LanguageId           -- R [ KW["id"] KW[":"] _1 ],
   Extensions           -- R [ KW["extensions"] KW[":"] _1 ],
   Description          -- R [ KW["description"] KW[":"] _1 ],
   Table                -- R [ KW["table"] KW[":"] _1 ],
   TableProvider        -- R [ KW["table"] KW["provider"] KW[":"] _1 ],
   UnmanagedTablePrefix -- R [ H [ KW["unmanaged"] KW["table"] ] KW[":"] H hs=0 [ _1 KW["*"] ] ],
   StartSymbols         -- R [ H [ KW["start"] KW["symbols"] ] KW[":"] _1 ],
   NoStartSymbols       -- KW["_"],
   URL                  -- R [ KW["url"] KW[":"] _1 ],
   Extends              -- R [ KW["extends"] KW[":"] _1 ],
   Aliases              -- R [ KW["aliases"] KW[":"] _1 ],
   LineCommentPrefix    -- R [ H [ KW["line"] KW["comment"] ] KW[":"] _1 ],
   BlockCommentDefs     -- R [ H [ KW["block"] KW["comment"] ] KW[":"] _1 ],
   FenceDefs            -- R [ KW["fences"] KW[":"] A(l,l) [ _1 ] ],
   FenceDef             -- R [ _1 _2 ],
   IndentDefs           -- R [ H [ KW["indent"] KW["after"]] KW[":"] V [ _1 ] ],
   IndentDef            -- _1,
   BlockCommentDef      -- H [ _1 _2 _3 ],
   String               -- _1,
   NoContinuation       --,
   CommentLine          -- R [ H hs=0 [ KW["//"] _1 ]],
   EmptyLine            -- R [KW[""]],
   Token                -- _1,
   TK_IDENTIFIER        -- KW["identifier"],
   TK_NUMBER            -- KW["number"],
   TK_LAYOUT            -- KW["layout"],
   TK_STRING            -- KW["string"],
   TK_KEYWORD           -- KW["keyword"],
   TK_OPERATOR          -- KW["operator"],
   TK_VAR               -- KW["var"],
   TK_JUNK              -- KW["junk"],
   TK_UNKNOWN           -- KW["unknown"],
   NORMAL               -- ,
   BOLD                 -- KW["bold"],
   ITALIC               -- KW["italic"],
   BOLD_ITALIC          -- H [KW["bold"] KW["italic"]],
   ColorDefault         -- KW["_"],
   NoColor              -- ,
   ColorRGB             -- H  [_1 _2 _3],
   Outliner             -- V is=2 [H hs=0  [KW["outliner"] _1] V [_2]],
   Outliner.2:iter-star -- _1,
   Folding              -- V is=2 [H hs=0  [KW["folding"] _1] V [_2]],
   Folding.2:iter-star  -- _1,
   Sort                 -- _1,
   ListSort             -- _1 KW["*"],
   ConstructorOnly      -- H hs=0 [KW["_"] KW["."] _1],
   Constructor          -- _1,
   SortAndConstructor   -- H hs=0 [_1 KW["."] _2],
   Module               -- V vs = 1 [H  [KW["module"] _1] _2 _3 ],
   Module.3:iter-star   -- _1,
   Imports              -- V is=2 [KW["imports"] _1],
   Imports.1:iter       -- _1,
   Import               -- _1,
   ImportRenamed        -- H hs=0 [_1 KW["["] _2 KW["]"]],
   NoImports            --
]

