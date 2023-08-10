# gLTSdiff library overview

The gLTSdiff library is designed with extensibility in mind, both for adding different model representations and for adding algorithms to compare them.
The following concepts are discussed in more detail on this page:

* **Representations**:
  gLTSdiff supports comparison and merging of various types of models.
  At the basis, a Generalized Labeled Transition System (GLTS), essentially a directed graph, is used.
  GLTSs allow arbitrary values to be used as state and transition properties.
  Specific representations may be introduced, for instance to represent Labeled Transition Systems (LTSs), Non-deterministic Finite Automata (NFAs), or Extended Finite Automata (EFAs).

* **Combiners**:
  Combiners form the basis for comparison of various types of model representations.
  Two combiners are needed, one for the state properties and one for the transition properties.
  A combiner for a certain type of properties indicates which property values may be combined, and if they may be combined, what their combination is.
  Different combiners may be used to customize comparison, and compare the same representation in different ways.

* **Matchers**:
  Matchers compute a matching between two input models, indicating which states of the first model map to which states in the second model.
  They make use of the state and transition property combiners, to ensure states are only matched if their state properties are combinable.

* **Scorers**:
  Scorers are used by some matchers to determine what are good matches.

* **Mergers**:
  Mergers allow to merge two input models into a single merged model, based on a matching computed by a matcher.

* **Rewriters**:
  Rewriters allow to optionally rewrite the merge results, as post-processing.
  Undesired patterns in the merged models may be rewritten to reduce the number of differences, or improve the readability.

* **Hiders**:
  Hiders are used by some rewriters to hide transition properties.

* **Inclusions**:
  Inclusion relations are used by some rewriters to determine property inclusion.

* **Writers**:
  Writers allow writing models to DOT format, which may subsequently be rendered as an image.

* **Printers**:
  Printers are used by writers to print state and transition properties to HTML text, for inclusion in DOT files.

* **Builders**:
  Builders hide many low-level details and provide a default configuration, allowing to perform comparisons using less code.
  Different builders are available for different representations.

* **Projectors**:
  Projectors allow to project state and transition properties along a certain value.
  Their use is currently mostly limited to testing.

Below these concepts are explained in more detail.

## Representations

gLTSdiff supports comparison and merging of various types of models.
At the basis is a Generalized Labeled Transition System (GLTS), which represents a directed graph.
The `GLTS` class is parameterized with the type of state and transition properties to use.
The class has various methods to manipulate GLTSs, such as adding and removing states and transitions.
It also has various methods to query GLTS, such as getting all its states, querying the incoming and outgoing transitions of a state, and so on.

States are represented by the `State` class.
Transitions are represented by the `Transition` class.
The `GLTSs` class has some methods to work with multiple GLTSs, such as computing common outgoing transitions for states from different GLTSs.

gLTSdiff by default includes a few specialized GLTS representations:

* Labeled Transitions Systems (LTSs) add initial state information to GLTS state properties.
  The `BaseLTS` class extends the `GLTS` class and requires the use of `LTSStateProperty` instances as state properties.
  The `BaseLTS` class allows also derived classes of `LTSStateProperty`, allowing other representations to extend upon it.
  To work with concrete LTSs, use the `LTS` class, which is fixed to using `LTSStateProperty` for state properties.

* Automata add accepting state information to LTS state properties.
  The `BaseAutomaton` class extends the `BaseLTS` class and requires the use of `AutomatonStateProperty` instances as state properties.
  The `BaseAutomaton` class allows also derived classes of `AutomatonStateProperty`, allowing other representations to extend upon it.
  To work with concrete automata, use the `Automaton` class, which is fixed to using `AutomatonStateProperty` for state properties.

* Difference automata, or just diff automata, add difference information to automaton state and transition properties.
  The `BaseDiffAutomaton` class extends the `BaseAutomaton` class and requires the use of `DiffAutomatonStateProperty` instances as state properties.
  The `BaseDiffAutomaton` class allows also derived classes of `DiffAutomatonStateProperty`, allowing other representations to extend upon it.
  To work with concrete difference automata, use the `DiffAutomaton` class, which is fixed to using `DiffAutomatonStateProperty` for state properties.
  `BaseDiffAutomaton` also enforces that transition properties are extended with difference kind information, using the `DiffProperty` class.
  Difference kinds are represented as an enum `DiffKind`, with values `UNCHANGED`, `ADDED` and `REMOVED`.
  The `DiffAutomata` class contains some utility functionality, such as functionality to convert a regular automaton to a diff automaton, or vice versa.

Additional representations can be added, either by extending `GLTS`, or by extending one of the built-in extensions, such as `BaseLTS`, `BaseAutomaton`, or `BaseDiffAutomaton`.
These built-in extensions serve as good examples of how to add additional representations.

## Combiners

Combiners form the basis for comparison of various types of model representations.
GLTSs are parameterized with a type of state properties and a type of transition properties.
Hence, two combiners are needed, one for the state properties and one for the transition properties.

A combiner for a certain type of property indicates which property values may be combined, and if they may be combined, what their combination is.
Different combiners may then be used to customize comparison, and compare the same representation in different ways.
For instance, one could configure a combiner that allows state acceptance information to match only if the information is the same for both states (they are either both accepting or both non-accepting).
Their combination would then simply be one of the properties, as only identical properties can then be merged.
Alternatively, one could configure a combiner that allow states to always be merged, and their merge to be an accepting state if either of the states being merged was accepting.
What is a good combiner for certain properties may depend on the particular use case.

All combiners extend the abstract `Combiner` base class.
It has two essential methods: `computeAreCombinable` and `computeCombination`.
The latter may only be invoked to combine two properties, if the former returns indicates that they are combinable.
The methods have some additional constraints for combiners to be valid.
See the JavaDocs for more information.

gLTSdiff ships with several basic combiners:

* `EqualityCombiner` combines any two properties only if they are equal to one another (as per `Object.equals`).
  The result of combing them is equal to both the input properties.
* `FixedValueCombiner` allows any two properties to be combined, always producing the same fixed value.

gLTSdiff also comes with several composite combiners:

* `OptionalCombiner` combines optional properties (Java's `Optional` class).
  Two optionals are always combinable.
  It is assumed that their inner properties are always combinable as well.
  The combination of two optionals is defined to be an optional with a combined inner property, or `Optional.empty()`
  if there are no inner properties.
* `PairCombiner` combines `org.apache.commons.math3.util.Pair` instances.
  Two pairs of properties are combinable if their left and right properties are pairwise combinable.
  The combination of two pairs is a pair with a combined left and right property.
* `SetCombiner` combines sets of properties.
  Sets can always be combined.
  Combining any two sets results in the union of these sets in which all combinable
  properties are combined.
* `SubtypeCombiner` combines `U`-typed properties based on a combiner for `T`-typed properties, where `U` is a subtype of `T`.
* `TransitionCombiner` combines `Transition`s.
  Any two transitions are combinable if their source and target states are equal and their properties are combinable.
  Combining two transitions results in a transition with a combined transition property.

gLTSdiff further comes with several combiners for state and transition properties of specific representations:

* `LTSStatePropertyCombiner` combines `LTSStateProperty` state properties.
  Any two such properties can be combined if they agree on states being initial (i.e., either both states are initial or both states are not initial).
  Combining two such properties results in an LTS state property with their equal initial state information.
* `AutomatonStatePropertyCombiner` combines `AutomatonStateProperty` state properties.
  Any two such properties can be combined if they agree on states being initial and accepting (i.e., either both states are initial and accepting or both states are not initial and not accepting).
  Combining two such properties results in an automaton state property with their equal initial state and state acceptance information.
* `DiffAutomatonStatePropertyCombiner` combines `DiffAutomatonStateProperty` state properties.
  Any two such properties are combinable if they agree on states being accepting (i.e., both states are accepting or both states are non-accepting).
  Combining two such properties results in a difference automaton state property with a combined state difference kind, combined initial state (difference) information, and their equal state acceptance information.
* `DiffPropertyCombiner` combines `DiffProperty` transition properties.
  Any two difference properties are combinable if their inner properties are combinable and their difference kinds are combinable.
  Combining two difference properties results in a difference property with a combined inner property and a combined difference kind.
* `DiffKindCombiner` combines `DiffKind` properties.
  Difference kinds can always be combined.
  Combining any two difference kinds results either in `UNCHANGED` if the inputs are unequal, or otherwise gives a result that is equal to the input operands.

Additional combiners may be defined, and the existing ones can serve as good examples.
However, it is not always necessary to add your own combiners.
The existing combiners may be used in different ways to define combiners for various types.
See for instance the [merging of version-annotated automata example](example-merge-three-version-annotated-automata.md) for how to configure combiners over the structure of the `Pair<String, Set<Integer>>` type, i.e., a combiner for the `Pair`s, for the `String`s, for the `Set`s and for the `Integer`s.

## Matchers

Matchers compute a matching between two input models.
A matching indicates which states of the first input model map to which states in the second input model.
All states that are in the matching are considered unchanged between the models, while states that are not in the mapping are considered removed (if they are only in the first input) or added (if they are only in the second input).
Matchers make use of the state and transition property combiners, to determine which states can be matched.
Matchers will only match states where the state property combiner indicates the state properties are combinable.
The JavaDoc of the `Matcher` interface lists some additional constraints to which matchings must adhere.

Many possible implementations of matchers exist that produce valid matchings.
This includes heuristic-based matchers as well as optimization-based matchers.
gLTSdiff comes with various built-in matchers:

* `WalkinshawMatcher` is a heuristic-based matcher that uses landmarks, 'obviously' matching state pairs, as [proposed](https://doi.org/10.1145/2430545.2430549) for the LTSdiff algorithm by Walkinshaw and Bogdanov.
  The gLTSdiff implementation has been generalized to work with combiners.
  This matcher relies on scores, and works well in practice.
* `KuhnMunkresMatcher` is an optimization-based matcher that implements the well-known Kuhn–Munkres algorithm (also called the Hungarian method).
  It produces a guaranteed maximal matching, with a maximum score.
  It can be quite computation-intensive, especially for large and dense GLTs, but may produce better matches than the `WalkinshawMatcher`.
* `BruteForceMatcher` is an optimization-based matcher that calculates a best possible maximal matching, without relying on scores.
  The results are *best possible* (or optimal) in the sense that matchings are computed with the objective to maximize the number of transitions that would be combined in the final merged GLTS.
  In other words, it minimizes the number of uncombined transitions (the number of differences).
  Since the algorithm explores all the possible choices of relevant state matchings (it is brute force), it is quite computation-intensive.
  It is therefore generally only a good choice for smaller models.
* `DynamicMatcher` allows to use different matchers for different comparisons, based on the sizes of the models being compared.
  By default, it uses a `KuhnMunkresMatcher` if both inputs have 45 states or less, and a `WalkinshawMatcher` otherwise, but this may be configured.
  The dynamic matcher allows for a trade-off between computation complexity (running time and memory) and the number of differences in the result.

Specific matchers are also present for certain representations, that take into account the additional information in state and transition properties of those representations:

* gLTSdiff comes with matchers for LTSs, that take into account the initial states.

Different matchers may be added by implementing the `Matcher` interface, or by extending the `ScoringMatcher` class for matchers based on scores.

## Scorers

Some matchers use similarity scorers.
Such scorers compute scores of how good two states from different GLTs match.
Matchers may then give preference to matching state pairs with higher scores.
Scores may take into account not only state properties, but also how similar the surrounding structures of the states are.

All similarity scorers implement the `SimilarityScorer` interface.
gLTSdiff comes with multiple built-in scorers:

* `WalkinshawGlobalScorer` computes global similarity scores between states, by transforming the problem of finding global similarity scores to a problem of solving a system of linear equations, as [proposed](https://doi.org/10.1145/2430545.2430549) for the LTSdiff algorithm by Walkinshaw and Bogdanov.
  The gLTSdiff implementation has been generalized to work with combiners.
  Computing global scores can be quite computation-intensive, especially for large and dense models, but generally produces good scores.
* `WalkinshawLocalScorer` computes local similarity scores between states.
  These scores are local in the sense that they are determined by the amount of overlap in incoming and outgoing transitions.
  By performing more than one refinement, this method allows taking further away neighbors into account, for better quality scoring, at higher computation costs.
  As more and more context is taken into account by increasing the number of refinements, it comes closer to the global scorer.
* `DynamicScorer` allows to use different scorers for different comparisons, based on the sizes of the models being compared.
  By default, if both inputs have at most 45 states, the global scorer is used.
  Otherwise, if both inputs have at most 500 states, the local scorer is used, with five refinements.
  Otherwise, the local scorer is used with a single refinement.
  However, the dynamic scorer can be configured differently, if desired.
  The dynamic scorer allows for a trade-off between computation complexity (running time and memory) and the quality of the scores.

Specific scorers are also present for certain representations, that take into account the additional information in state and transition properties of those representations:

* gLTSdiff comes with scorers for LTSs, that take into account the initial states.

Different scorers may be added by implementing the `SimilarityScorer` interface.

## Mergers

Mergers allow to merge two input models into a single merged model, based on a matching computed by a matcher.
Matched states and transitions are merged into single states and transitions, combining their properties using combiners.
Unmatched states and transitions are kept as is.

All mergers implement the `Merger` instance.
gLTSdiff ships with a single built-in merger, the `DefaultMerger`.
Likely this merger suffices for all use cases, and no custom mergers need to be implemented.

## Rewriters

Rewriters allow to optionally rewrite the merge results, as post-processing.

Ideally, a result of structural comparison has as few differences as possible, that are immediately apparent.
However, even with an optimization algorithm producing optimal matches, there may be 'undesired' differences.
Rewriters allow to optionally rewrite the merge results, as post-processing.
They may rewrite undesired patterns in the merged models, to reduce the number of differences, or improve the readability.

gLTSdiff comes with several built-in rewriters:

* `NothingRewriter` is a rewriter that does not perform any rewriting.
* `LocalRedundancyRewriter` eliminates patterns of local redundancy in GLTSs.
  A *pattern of local redundancy* consists of two or more transitions with combinable properties, that all share the same source state and target state.
  This rewriter merges such transitions into a single combined transition.
* `EntanglementRewriter` is a rewriter that rewrites tangles in difference automata.
  Tangles are poorly matched (and thus merged) states with incoming and outgoing transitions of both inputs, where none of those transitions got merged.
  This rewriter 'untangles' them, by splitting the tangle state into two new states, one with the added incoming/outgoing transitions, and one with the removed incoming/outgoing transitions.
* `SkipForkPatternRewriter` and `SkipJoinPatternRewriter` rewrite skip patterns in difference automata.
  Skip patterns are patterns where one input skips a part of a sequence that the other input does allow.
  Comparing such models leads to difference automata where either the transition before the skipped behavior is duplicated (fork pattern), or the one after it (join pattern).
  This rewriter rewrites such patterns, combining the common transitions, and adding a new 'skip' transition that indicates that a part of the sequence is only possible in one of the models.
  The [post-processing example](example-post-process-diff-automata.md) shows this in more detail.
* `SequenceRewriter` is a rewriter that applies multiple rewriters in sequence.
* `FixedPointRewriter` is a rewriter that repeatedly applies another rewriter until it no longer changes the GLTS.

Additional rewriters may be added by implementing the `Rewriter` interface, or extending one of the abstract classes that implement the interface.

## Hiders

Hiders are used by some rewriters to hide transition properties.
For instance, the `SkipForkPatternRewriter` and `SkipJoinPatternRewriter` use hiders to determine the transition properties for newly added `skip` transitions.

All hiders implement the `Hider` interface.
gLTSdiff ships with several built-in hiders:

* `SubstitutionHider` hides properties simply by replacing them by a specified non-`null` substitute.
* `DiffPropertyHider` hides `DiffProperty<T>`-typed properties by hiding their inner `T`-typed properties and leaving the associated `DiffKind` unchanged.

Additional hiders may be added by implementing the `Hider` interface.

## Inclusions

Inclusion relations are used by some rewriters to determine property inclusion.
For instance, the `SkipForkPatternRewriter` and `SkipJoinPatternRewriter` use an inclusion relation when transforming skip patterns.

gLTSdiff ships with an `EqualToCombinationInclusion` relation that should suffice in most cases.
Additional inclusion relations may be added by implementing the `Inclusion` interface, but it is recommended to extend from the `BaseInclusion` class instead.

## Writers

Writers allow writing models to DOT format, which may subsequently be rendered as an image.
`DotWriter` allows to write GLTs, and requires state and transition label printers to print the state and transition properties to HTML.
The DOT 'file' may be written to a file or to an `OutputStream`.
Customized DOT writers exist for specific representations:

* `LTSDotWriter` for LTSs.
* `AutomatonDotWriter` for automata.
* `DiffAutomatonDotWriter` for difference automata.

Once a model is written to a DOT file, the `DotRenderer` can be used to render the DOT file as an SVG image.
The `DotRenderer` requires [GraphViz](dependency-graphviz.md) for this.

## Printers

Printers are used by writers to print state and transition properties to HTML text, for inclusion in DOT files.
gLTSdiff ships with various built-in printers:

* `PairHtmlPrinter` prints `org.apache.commons.math3.util.Pair` instances.
  It allows to customize the prefix text, infix/separator text, and suffix text.
  It also allows to customize the printers to use for the first and second elements of the pairs.
* `SetHtmlPrinter` prints sets.
  It allows to customize the prefix text, separator/delimiter text, and suffix text.
  It also allows to customize the printer to use for the elements of the set.
* `StateHtmlPrinter` prints states, based on state IDs, and a custom prefix.
* `StringHtmlPrinter` prints properties by converting them to strings using `Object.toString()` and applying HTML escaping.
* `SubtypeHtmlPrinter` prints `U`-typed properties based on a printer for `T`-typed properties, where `U` is a
  subtype of `T`.
* `TransitionHtmlPrinter` prints transitions based on a printer for transition properties.

Additional printers may be added by implementing the `HtmlPrinter` interface.
However, it is not always necessary to add your own printers.
The existing printers may be used in different ways to define printers for various types.
See for instance the [merging of version-annotated automata example](example-merge-three-version-annotated-automata.md) for how to configure printers over the structure of the `Pair<String, Set<Integer>>` type, i.e., a printer for the `Pair`s, for the `String`s, for the `Set`s and for the `Integer`s.

## Builders

Structural comparison and merging can be performed using the `StructureComparator` class.
However, using that class directly can be quite cumbersome, as it requires quite some configuration.
Also, direct usage of this class often leads to quite some complex use of Java generics.
Builders hide many of the low-level details and provide a default configuration, allowing to perform comparisons using less code.
Different builders are available for different representations:

* `BaseStructureComparatorBuilder` is a builder for comparing `GLTS`s and derived classes.
  `StructureComparatorBuilder` is a builder for comparing concrete `GLTS` instances.
* `BaseLTSStructureComparatorBuilder` is a builder for comparing `BaseLTS`s and derived classes.
  `LTSStructureComparatorBuilder` is a builder for comparing concrete `LTS` instances.
* `BaseAutomatonStructureComparatorBuilder` is a builder for comparing `BaseAutomaton` and derived classes.
  `AutomatonStructureComparatorBuilder` is a builder for comparing concrete `Automaton` instances.
* `BaseDiffAutomatonStructureComparatorBuilder` is a builder for comparing `BaseDiffAutomaton` and derived classes.
  `DiffAutomatonStructureComparatorBuilder` is a builder for comparing concrete `DiffAutomaton` instances.

A builder is typically used as follows:

* Create an instance of the builder.
* Optionally, invoke various `set*` methods to configure the builder.
* Invoke `createComparator` to get a `StructureComparator` to perform comparison, merging, and post-processing.
* Invoke `createWriter` to get a `DotWriter` (or an instance of a derived class) to write models to DOT format.
  It can typically be used to write both the input models and the resulting output models.

Builders offer a lot of configuration via their `set*` methods.
See the JavaDoc of the builders for details.
Here we list only some examples:

* Invoke `setStatePropertyCombiner` to set a custom state property combiner.
  Invoke `setDefaultStatePropertyCombiner` to restore the default state property combiner.
* Invoke `setWalkinshawLocalScorer` to use the Walkinshaw local scorer.
* Invoke `setTransitionPropertyHider` to set a transition property hider.

Similar to the builders for existing representations, additional builders may be added for other representations.
The existing builders serve as good examples.

## Projectors

Projectors allow to project state and transition properties along a certain value.
For instance, a difference automaton may be projected along a difference kind to produce a regular automaton containing only the states and transitions of that difference kind.
The use of projectors is currently mostly limited to testing.

gLTSdiff ships with various built-in projectors:

* `IdentityProjector` returns the given input property unaltered.
* `OptionalProjector` projects `Optional<T>` instances, by projecting their contained properties, if present.
* `SetProjector` projects sets, by projecting each of the elements of the set.
* `SubtypeProjector` projects `U`-typed properties based on a projector for `T`-typed properties, where `U` a subtype of `T`.

Additional projectors may be added by implementing the `Projector` interface.
