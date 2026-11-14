using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using UnityEngine.EventSystems;
using UnityEngine.UI;

namespace BathtimeBucks
{
    public enum SlotSymbol
    {
        Wild,
        Duck,
        Boat,
        Soap,
        Shampoo,
        Bell,
        TripleBar,
        DoubleBar,
        SingleBar,
        Watermelon,
        Plum,
        Orange,
        Cherry
    }

    public readonly struct SymbolSpec
    {
        public readonly SlotSymbol Symbol;
        public readonly string DisplayName;
        public readonly int Weight;
        public readonly int Pay2;
        public readonly int Pay3;
        public readonly int Pay4;
        public readonly int Pay5;
        public readonly Color AccentColor;
        public readonly bool IsBar;

        public SymbolSpec(
            SlotSymbol symbol,
            string displayName,
            int weight,
            int pay2,
            int pay3,
            int pay4,
            int pay5,
            Color accentColor,
            bool isBar = false)
        {
            Symbol = symbol;
            DisplayName = displayName;
            Weight = weight;
            Pay2 = pay2;
            Pay3 = pay3;
            Pay4 = pay4;
            Pay5 = pay5;
            AccentColor = accentColor;
            IsBar = isBar;
        }
    }

    public sealed class PaylineDef
    {
        public readonly int Index;
        public readonly string Name;
        public readonly int[] Rows;
        public readonly Color LineColor;

        public PaylineDef(int index, string name, int[] rows, Color lineColor)
        {
            Index = index;
            Name = name;
            Rows = rows;
            LineColor = lineColor;
        }
    }

    public sealed class WinningLineResult
    {
        public PaylineDef Payline;
        public SlotSymbol Symbol;
        public int MatchCount;
        public long PayoutCents;
        public bool IsProgressive;
    }

    public sealed class EvaluationResult
    {
        public long TotalPayoutCents;
        public List<WinningLineResult> WinningLines = new List<WinningLineResult>();
        public int ScatterBonusSpins;
        public HashSet<(int col, int row)> BonusTriggerCells = new HashSet<(int col, int row)>();
        public bool ProgressiveWon;
    }

    public sealed class SkillPromptData
    {
        public int TargetColumn;
        public int WinningRow;
        public SlotSymbol TargetSymbol;
        public List<SlotSymbol> CandidateSymbols = new List<SlotSymbol>();
        public long PotentialWinCents;
        public bool IsSolved;
    }

    /// <summary>
    /// Self-contained 1-to-1 Unity C# port of Bathtime Bucks.
    /// Drop this single file into Assets/, attach to any GameObject (or enter Play Mode),
    /// and it builds the full 5x3 arcade cabinet UI, procedural sprites, and AudioClip SFX.
    /// </summary>
    public class BathtimeBucksUnity : MonoBehaviour
    {
        [Header("Optional Custom Overrides (Auto-Generated if Left Empty)")]
        [SerializeField] private Sprite[] customSymbolSprites = new Sprite[13];
        [SerializeField] private AudioClip customSpinLoopClip;
        [SerializeField] private AudioClip customReelStopClip;
        [SerializeField] private AudioClip customWinClip;
        [SerializeField] private AudioClip customBonusClip;

        // Constants & Paytable
        private static readonly long[] WagerOptionsCents = { 25L, 50L, 100L, 200L };
        private const long MaxWagerCents = 200L;
        private const long ProgressiveSeedCents = 250000L;

        private static readonly Dictionary<SlotSymbol, SymbolSpec> Specs = new Dictionary<SlotSymbol, SymbolSpec>
        {
            { SlotSymbol.Wild,       new SymbolSpec(SlotSymbol.Wild,       "WILD",        4,  0, 25, 100, 500, new Color(0.11f, 0.71f, 0.88f)) },
            { SlotSymbol.Duck,       new SymbolSpec(SlotSymbol.Duck,       "RUBBER DUCK", 6,  0, 15,  60, 250, new Color(1.00f, 0.83f, 0.00f)) },
            { SlotSymbol.Boat,       new SymbolSpec(SlotSymbol.Boat,       "TOY BOAT",    7,  0, 10,  40, 150, new Color(1.00f, 0.34f, 0.13f)) },
            { SlotSymbol.Soap,       new SymbolSpec(SlotSymbol.Soap,       "SOAP BAR",    8,  0,  8,  30, 100, new Color(0.22f, 0.74f, 0.97f)) },
            { SlotSymbol.Shampoo,    new SymbolSpec(SlotSymbol.Shampoo,    "LUCKY BONUS", 6,  0,  5,  15,  50, new Color(0.13f, 0.77f, 0.37f)) },
            { SlotSymbol.Bell,       new SymbolSpec(SlotSymbol.Bell,       "GOLD BELL",   9,  0,  6,  20,  75, new Color(1.00f, 0.83f, 0.00f)) },
            { SlotSymbol.TripleBar,  new SymbolSpec(SlotSymbol.TripleBar,  "3X BAR",     10,  0,  5,  15,  50, new Color(0.08f, 0.50f, 0.24f), true) },
            { SlotSymbol.DoubleBar,  new SymbolSpec(SlotSymbol.DoubleBar,  "2X BAR",     11,  0,  4,  12,  35, new Color(0.01f, 0.52f, 0.78f), true) },
            { SlotSymbol.SingleBar,  new SymbolSpec(SlotSymbol.SingleBar,  "1X BAR",     12,  0,  3,   8,  25, new Color(0.85f, 0.47f, 0.02f), true) },
            { SlotSymbol.Watermelon, new SymbolSpec(SlotSymbol.Watermelon, "MELON",      13,  0,  3,   7,  20, new Color(0.94f, 0.27f, 0.27f)) },
            { SlotSymbol.Plum,       new SymbolSpec(SlotSymbol.Plum,       "PLUM",       14,  0,  2,   5,  15, new Color(0.66f, 0.33f, 0.97f)) },
            { SlotSymbol.Orange,     new SymbolSpec(SlotSymbol.Orange,     "ORANGE",     15,  0,  2,   4,  12, new Color(0.98f, 0.45f, 0.09f)) },
            { SlotSymbol.Cherry,     new SymbolSpec(SlotSymbol.Cherry,     "CHERRIES",   16,  1,  2,   5,  12, new Color(0.86f, 0.15f, 0.15f)) },
        };

        private static readonly PaylineDef[] AllPaylines =
        {
            new PaylineDef(1, "Center Line", new[] { 1, 1, 1, 1, 1 }, new Color(1.00f, 0.83f, 0.00f)),
            new PaylineDef(2, "Top Line",    new[] { 0, 0, 0, 0, 0 }, new Color(0.22f, 0.74f, 0.97f)),
            new PaylineDef(3, "Bottom Line", new[] { 2, 2, 2, 2, 2 }, new Color(0.13f, 0.77f, 0.37f)),
            new PaylineDef(4, "V-Shape",     new[] { 0, 1, 2, 1, 0 }, new Color(0.98f, 0.45f, 0.09f)),
            new PaylineDef(5, "Inverted V",  new[] { 2, 1, 0, 1, 2 }, new Color(0.91f, 0.47f, 0.98f)),
            new PaylineDef(6, "Top Dip",     new[] { 0, 0, 1, 0, 0 }, new Color(0.96f, 0.25f, 0.37f)),
            new PaylineDef(7, "Bottom Rise", new[] { 2, 2, 1, 2, 2 }, new Color(0.08f, 0.72f, 0.65f)),
            new PaylineDef(8, "Step Down",   new[] { 0, 1, 1, 1, 2 }, new Color(0.98f, 0.80f, 0.08f)),
            new PaylineDef(9, "Step Up",     new[] { 2, 1, 1, 1, 0 }, new Color(0.38f, 0.65f, 0.98f)),
        };

        // Persistent Game State
        private long _balanceCents = 100000L;
        private long _wagerCents = 100L;
        private long _progressiveCents = 253759L;
        private long _winCents;
        private int _bonusSpinsRemaining;
        private int _totalSpins;
        private long _totalWonCents;
        private long _biggestWinCents;
        private bool _soundEnabled = true;
        private bool _skillModeEnabled = true;

        // Runtime State
        private readonly SlotSymbol[,] _grid = new SlotSymbol[5, 3];
        private readonly bool[] _reelSpinning = new bool[5];
        private readonly float[] _reelScrollPhase = new float[5];
        private readonly float[] _reelBounceOffset = new float[5];
        private readonly List<SlotSymbol>[] _reelStrips = new List<SlotSymbol>[5];
        private bool _fastStopRequested;
        private SkillPromptData _activeSkillPrompt;
        private EvaluationResult _lastEval;
        private readonly List<SlotSymbol> _weightedPool = new List<SlotSymbol>();

        // Audio
        private AudioSource _sfxSource;
        private AudioSource _loopSource;
        private AudioClip _spinStartClip;
        private AudioClip _spinLoopClip;
        private AudioClip _reelStopClip;
        private AudioClip _winSmallClip;
        private AudioClip _winBigClip;
        private AudioClip _bonusClip;
        private AudioClip _bubblePopClip;
        private AudioClip _buttonTapClip;

        // Procedural UI & Sprites
        private Font _font;
        private readonly Dictionary<SlotSymbol, Sprite> _sprites = new Dictionary<SlotSymbol, Sprite>();
        private Sprite _circleSprite;

        // UI References
        private Text _progressiveText;
        private Text _progressiveTagText;
        private GameObject _bonusSpinsBadge;
        private Text _bonusSpinsBadgeText;
        private Text _statusBannerText;
        private Text _wagerText;
        private Text _winText;
        private Text _balanceText;
        private Text _playButtonText;
        private Image _playButtonBg;
        private GameObject _instantWinPlaque;
        private Text _instantWinAmountText;

        private readonly Image[,] _staticCellImages = new Image[5, 3];
        private readonly Image[,] _staticCellBorders = new Image[5, 3];
        private readonly GameObject[,] _bonusCellTags = new GameObject[5, 3];
        private readonly RectTransform[,] _staticCellRects = new RectTransform[5, 3];
        private readonly List<Image[]> _spinningSlotImages = new List<Image[]>();
        private readonly List<RectTransform[]> _spinningSlotRects = new List<RectTransform[]>();
        private readonly float[] _reelHeights = new float[5];

        // Overlays
        private GameObject _skillOverlayRoot;
        private RectTransform _skillOverlayRect;
        private readonly Image[] _skillChoiceImages = new Image[3];
        private GameObject _celebrationOverlayRoot;
        private Text _celebrationTitleText;
        private Text _celebrationSubText;
        private Text _celebrationSpinsText;
        private Text _celebrationAmountText;
        private RectTransform _celebrationRaysRect;
        private readonly List<RectTransform> _celebrationParticles = new List<RectTransform>();
        private readonly List<Vector2> _particleDirs = new List<Vector2>();
        private readonly List<float> _particleSpeeds = new List<float>();

        private GameObject _prizeModalRoot;
        private Text _prizeModalBodyText;
        private GameObject _collectModalRoot;
        private Text _collectModalBodyText;
        private GameObject _settingsModalRoot;
        private Text _soundToggleLabel;
        private Text _skillToggleLabel;

        private Coroutine _spinCoroutine;
        private Coroutine _celebrationCoroutine;

        private bool IsAnyReelSpinning
        {
            get
            {
                for (int i = 0; i < 5; i++)
                    if (_reelSpinning[i]) return true;
                return false;
            }
        }

        private void Awake()
        {
            _font = GetBuiltinFont();
            BuildWeightedPool();
            InitDefaultGrid();
            BuildReelStrips();
            LoadPrefs();
            GenerateProceduralSprites();
            SetupAudio();
            BuildCabinetUI();
            RefreshAllUI();
        }

        private void Update()
        {
            float dt = Mathf.Min(Time.deltaTime, 0.05f);
            for (int col = 0; col < 5; col++)
            {
                float cellH = _reelHeights[col] > 1f ? _reelHeights[col] / 3f : 120f;
                if (_reelSpinning[col])
                {
                    float speed = 17.5f + col * 1.1f;
                    _reelScrollPhase[col] += dt * speed;
                    int baseIdx = Mathf.FloorToInt(_reelScrollPhase[col]);
                    float frac = _reelScrollPhase[col] - baseIdx;
                    var strip = _reelStrips[col];

                    for (int slotIdx = 0; slotIdx < 4; slotIdx++)
                    {
                        int slot = slotIdx - 1; // -1..2
                        int stripIdx = Mod(baseIdx - slot, strip.Count);
                        _spinningSlotImages[col][slotIdx].gameObject.SetActive(true);
                        _spinningSlotImages[col][slotIdx].sprite = _sprites[strip[stripIdx]];
                        float y = -(slot + frac) * cellH;
                        _spinningSlotRects[col][slotIdx].anchoredPosition = new Vector2(0f, y);
                    }
                    for (int row = 0; row < 3; row++)
                    {
                        _staticCellImages[col, row].gameObject.SetActive(false);
                        _staticCellBorders[col, row].enabled = false;
                        _bonusCellTags[col, row].SetActive(false);
                    }
                }
                else
                {
                    if (_reelBounceOffset[col] < 0f)
                    {
                        _reelBounceOffset[col] = Mathf.MoveTowards(_reelBounceOffset[col], 0f, dt * 4.5f);
                    }
                    for (int slotIdx = 0; slotIdx < 4; slotIdx++)
                    {
                        _spinningSlotImages[col][slotIdx].gameObject.SetActive(false);
                    }
                    float bouncePx = _reelBounceOffset[col] * cellH;
                    for (int row = 0; row < 3; row++)
                    {
                        _staticCellImages[col, row].gameObject.SetActive(true);
                        _staticCellRects[col, row].anchoredPosition = new Vector2(0f, -row * cellH - bouncePx);
                    }
                }
            }

            if (_celebrationOverlayRoot != null && _celebrationOverlayRoot.activeSelf)
            {
                if (_celebrationRaysRect != null)
                {
                    _celebrationRaysRect.Rotate(0f, 0f, -45f * dt);
                }
                for (int i = 0; i < _celebrationParticles.Count; i++)
                {
                    var rt = _celebrationParticles[i];
                    Vector2 pos = rt.anchoredPosition + _particleDirs[i] * (_particleSpeeds[i] * dt);
                    pos.y += 42f * dt;
                    if (pos.magnitude > 650f) pos = Vector2.zero;
                    rt.anchoredPosition = pos;
                }
            }
        }

        private void OnDestroy()
        {
            SavePrefs();
        }

        // ---------------------------------------------------------------------
        // GAMEPLAY LOGIC (1-to-1 with Android BathtimeViewModel & Paylines)
        // ---------------------------------------------------------------------

        private void BuildWeightedPool()
        {
            _weightedPool.Clear();
            foreach (var kv in Specs)
            {
                for (int i = 0; i < kv.Value.Weight; i++)
                {
                    _weightedPool.Add(kv.Key);
                }
            }
        }

        private SlotSymbol RandomWeighted()
        {
            return _weightedPool[UnityEngine.Random.Range(0, _weightedPool.Count)];
        }

        private void InitDefaultGrid()
        {
            SlotSymbol[,] initial =
            {
                { SlotSymbol.SingleBar, SlotSymbol.Duck,      SlotSymbol.TripleBar },
                { SlotSymbol.Bell,      SlotSymbol.Cherry,    SlotSymbol.Boat },
                { SlotSymbol.Cherry,    SlotSymbol.Wild,      SlotSymbol.Soap },
                { SlotSymbol.Plum,      SlotSymbol.TripleBar, SlotSymbol.Boat },
                { SlotSymbol.TripleBar, SlotSymbol.Duck,      SlotSymbol.Watermelon },
            };
            for (int c = 0; c < 5; c++)
                for (int r = 0; r < 3; r++)
                    _grid[c, r] = initial[c, r];
        }

        private void BuildReelStrips()
        {
            var all = (SlotSymbol[])Enum.GetValues(typeof(SlotSymbol));
            for (int col = 0; col < 5; col++)
            {
                var list = new List<SlotSymbol>(20);
                for (int i = 0; i < 20; i++)
                {
                    list.Add(all[(i * 3 + col * 5) % all.Length]);
                }
                _reelStrips[col] = list;
            }
        }

        public void OnPlayClick()
        {
            if (_celebrationOverlayRoot.activeSelf)
            {
                DismissBonusCelebration();
                return;
            }
            if (IsAnyReelSpinning)
            {
                _fastStopRequested = true;
                return;
            }
            if (_activeSkillPrompt != null && !_activeSkillPrompt.IsSolved)
            {
                PlaySfx(_buttonTapClip, 0.65f);
                SetStatus("TAP THE WINNING ICON OR NUDGE ARROW!");
                return;
            }

            bool isBonusSpin = _bonusSpinsRemaining > 0;
            if (!isBonusSpin && _balanceCents < _wagerCents)
            {
                PlaySfx(_buttonTapClip, 0.65f);
                OpenCollectModal();
                SetStatus("LOW CREDITS - TAP REFILL TO CONTINUE");
                return;
            }

            if (_spinCoroutine != null) StopCoroutine(_spinCoroutine);
            _fastStopRequested = false;
            _spinCoroutine = StartCoroutine(ExecuteSpinRoutine(isBonusSpin));
        }

        public void OnMaxClick()
        {
            if (IsAnyReelSpinning || _bonusSpinsRemaining > 0) return;
            _wagerCents = MaxWagerCents;
            RefreshAllUI();
            OnPlayClick();
        }

        public void OnStepWager(int delta)
        {
            if (IsAnyReelSpinning || _bonusSpinsRemaining > 0) return;
            int idx = Array.IndexOf(WagerOptionsCents, _wagerCents);
            if (idx < 0) idx = 2;
            idx = Mathf.Clamp(idx + delta, 0, WagerOptionsCents.Length - 1);
            _wagerCents = WagerOptionsCents[idx];
            PlaySfx(_buttonTapClip, 0.65f);
            SetStatus(_wagerCents >= MaxWagerCents
                ? "MAX PLAY ACTIVE - PROGRESSIVE ELIGIBLE!"
                : "PLAY AMOUNT " + FormatCents(_wagerCents));
            SavePrefs();
            RefreshAllUI();
        }

        private IEnumerator ExecuteSpinRoutine(bool isBonusSpin)
        {
            if (!isBonusSpin) _balanceCents -= _wagerCents;
            else _bonusSpinsRemaining = Mathf.Max(0, _bonusSpinsRemaining - 1);

            long progInc = Math.Max(1L, _wagerCents * 3L / 100L);
            _progressiveCents += progInc;
            _winCents = 0L;
            _lastEval = null;
            _activeSkillPrompt = null;
            _skillOverlayRoot.SetActive(false);
            _instantWinPlaque.SetActive(false);
            _totalSpins++;

            for (int c = 0; c < 5; c++)
            {
                _reelSpinning[c] = true;
                _reelBounceOffset[c] = 0f;
            }
            SetStatus(isBonusSpin ? "BONUS SPIN - 2X MULTIPLIER!" : "GOOD LUCK!");
            RefreshAllUI();

            PlaySfx(_spinStartClip, 0.75f);
            StartSpinLoop();

            // Target grid
            for (int c = 0; c < 5; c++)
                for (int r = 0; r < 3; r++)
                    _grid[c, r] = RandomWeighted();

            if (isBonusSpin && UnityEngine.Random.value < 0.45f)
            {
                _grid[UnityEngine.Random.Range(0, 3), UnityEngine.Random.Range(0, 3)] = SlotSymbol.Wild;
            }

            float[] stopDurations = { 0.58f, 0.26f, 0.26f, 0.26f, 0.28f };
            for (int col = 0; col < 5; col++)
            {
                float target = _fastStopRequested ? 0.055f : stopDurations[col];
                float elapsed = 0f;
                while (elapsed < target && !_fastStopRequested)
                {
                    yield return null;
                    elapsed += Time.deltaTime;
                }
                if (_fastStopRequested && col > 0) yield return new WaitForSeconds(0.045f);

                _reelSpinning[col] = false;
                _reelBounceOffset[col] = -0.60f;
                if (col == 4) StopSpinLoop();
                PlaySfx(_reelStopClip, 0.85f, 0.94f + col * 0.04f);
                ApplyGridVisuals();
            }
            yield return new WaitForSeconds(0.22f);

            // Hot Swap Check
            if (HasDuckOrWild() && UnityEngine.Random.value < (isBonusSpin ? 0.28f : 0.14f))
            {
                var spots = PickHotSwapSpots();
                if (spots.Count > 0)
                {
                    SetStatus("HOT SWAP! DUCK WILDS INCOMING!");
                    PlaySfx(_bubblePopClip, 0.82f);
                    yield return new WaitForSeconds(0.38f);
                    foreach (var s in spots) _grid[s.col, s.row] = SlotSymbol.Wild;
                    PlaySfx(_bonusClip, 0.9f);
                    ApplyGridVisuals();
                    yield return new WaitForSeconds(0.22f);
                }
            }

            int mult = isBonusSpin ? 2 : 1;
            var eval = EvaluateGrid(_wagerCents, _wagerCents >= MaxWagerCents, _progressiveCents, mult);

            if (eval.TotalPayoutCents == 0L && _skillModeEnabled)
            {
                var prompt = DetectOrBuildSkillPrompt(_wagerCents, mult);
                if (prompt != null)
                {
                    _activeSkillPrompt = prompt;
                    PlaySfx(_bubblePopClip, 0.82f);
                    ShowSkillOverlay(prompt);
                    SetStatus("CHOOSE ICON OR NUDGE REEL " + (prompt.TargetColumn + 1) + " TO WIN!");
                    SavePrefs();
                    RefreshAllUI();
                    yield break;
                }
            }

            FinalizeOutcome(eval, false, isBonusSpin);
        }

        private void FinalizeOutcome(EvaluationResult eval, bool isSkillAward, bool wasBonusSpin)
        {
            _lastEval = eval;
            long payout = eval.TotalPayoutCents;
            _balanceCents += payout;
            _winCents = payout;
            _totalWonCents += payout;
            if (payout > _biggestWinCents) _biggestWinCents = payout;
            if (eval.ProgressiveWon) _progressiveCents = ProgressiveSeedCents;

            _bonusSpinsRemaining += eval.ScatterBonusSpins;

            int duckCount = 0;
            for (int c = 0; c < 5; c++)
                for (int r = 0; r < 3; r++)
                    if (_grid[c, r] == SlotSymbol.Duck) duckCount++;

            bool grand = eval.ScatterBonusSpins > 0 || eval.ProgressiveWon;

            string banner;
            if (eval.ProgressiveWon) banner = "PROGRESSIVE TOP PRIZE! +" + FormatCents(payout);
            else if (eval.ScatterBonusSpins > 0) banner = "DUCKY BONUS! +" + eval.ScatterBonusSpins + " FREE SPINS!";
            else if (isSkillAward) banner = "CORRECT ICON CHOSEN! WIN " + FormatCents(payout);
            else if (payout > 0L) banner = "WINNER! " + FormatCents(payout);
            else if (_bonusSpinsRemaining > 0) banner = "BONUS SPINS REMAINING: " + _bonusSpinsRemaining;
            else if (wasBonusSpin) banner = "BONUS ROUND COMPLETE!";
            else banner = "GOOD LUCK!";

            SetStatus(banner);

            if (grand)
            {
                PlaySfx(_bonusClip, 1.0f);
                if (eval.ProgressiveWon) PlaySfx(_winBigClip, 0.95f);
                ShowGrandCelebration(eval.ScatterBonusSpins, payout, eval.ProgressiveWon, duckCount);
            }
            else if (payout > 0L)
            {
                PlaySfx(payout >= _wagerCents * 4L ? _winBigClip : _winSmallClip, 0.92f);
                if (payout >= _wagerCents * 3L)
                {
                    _instantWinPlaque.SetActive(true);
                    _instantWinAmountText.text = FormatCents(payout);
                }
            }

            SavePrefs();
            RefreshAllUI();

            if (!grand && _bonusSpinsRemaining > 0)
            {
                StartCoroutine(AutoTriggerNextBonusSpin(payout > 0L ? 1.4f : 0.9f));
            }
        }

        private IEnumerator AutoTriggerNextBonusSpin(float delaySec)
        {
            yield return new WaitForSeconds(delaySec);
            if (!IsAnyReelSpinning && _bonusSpinsRemaining > 0 && _activeSkillPrompt == null && !_celebrationOverlayRoot.activeSelf)
            {
                OnPlayClick();
            }
        }

        private EvaluationResult EvaluateGrid(long wagerCents, bool progressiveEligible, long progressiveCents, int bonusMult)
        {
            var res = new EvaluationResult();
            long lineUnit = Math.Max(5L, wagerCents / 5L);

            foreach (var pl in AllPaylines)
            {
                SlotSymbol[] lineSyms = new SlotSymbol[5];
                for (int c = 0; c < 5; c++) lineSyms[c] = _grid[c, pl.Rows[c]];

                bool allWild = true;
                for (int c = 0; c < 5; c++) if (lineSyms[c] != SlotSymbol.Wild) { allWild = false; break; }
                if (allWild)
                {
                    long payout = progressiveEligible ? progressiveCents : Specs[SlotSymbol.Wild].Pay5 * lineUnit * bonusMult;
                    if (progressiveEligible) res.ProgressiveWon = true;
                    res.WinningLines.Add(new WinningLineResult
                    {
                        Payline = pl,
                        Symbol = SlotSymbol.Wild,
                        MatchCount = 5,
                        PayoutCents = payout,
                        IsProgressive = progressiveEligible
                    });
                    res.TotalPayoutCents += payout;
                    continue;
                }

                SlotSymbol? firstNonWild = null;
                for (int c = 0; c < 5; c++)
                {
                    if (lineSyms[c] != SlotSymbol.Wild && lineSyms[c] != SlotSymbol.Shampoo)
                    {
                        firstNonWild = lineSyms[c];
                        break;
                    }
                }

                if (firstNonWild.HasValue)
                {
                    int matchCount = 0;
                    for (int c = 0; c < 5; c++)
                    {
                        if (lineSyms[c] == firstNonWild.Value || lineSyms[c] == SlotSymbol.Wild) matchCount++;
                        else break;
                    }
                    var spec = Specs[firstNonWild.Value];
                    int mult = matchCount == 2 ? spec.Pay2 : matchCount == 3 ? spec.Pay3 : matchCount == 4 ? spec.Pay4 : matchCount == 5 ? spec.Pay5 : 0;
                    if (mult > 0)
                    {
                        long payout = mult * lineUnit * bonusMult;
                        res.WinningLines.Add(new WinningLineResult { Payline = pl, Symbol = firstNonWild.Value, MatchCount = matchCount, PayoutCents = payout });
                        res.TotalPayoutCents += payout;
                        continue;
                    }
                }

                int barCount = 0;
                for (int c = 0; c < 5; c++)
                {
                    if (Specs[lineSyms[c]].IsBar || lineSyms[c] == SlotSymbol.Wild) barCount++;
                    else break;
                }
                if (barCount >= 3)
                {
                    int barMult = barCount == 3 ? 2 : barCount == 4 ? 5 : 15;
                    long payout = barMult * lineUnit * bonusMult;
                    res.WinningLines.Add(new WinningLineResult { Payline = pl, Symbol = SlotSymbol.SingleBar, MatchCount = barCount, PayoutCents = payout });
                    res.TotalPayoutCents += payout;
                }
            }

            // 3+ Ducks anywhere OR 3+ Shampoo anywhere trigger Bonus Spins
            var duckCells = new List<(int, int)>();
            var shampooCells = new List<(int, int)>();
            for (int c = 0; c < 5; c++)
            {
                for (int r = 0; r < 3; r++)
                {
                    if (_grid[c, r] == SlotSymbol.Duck) duckCells.Add((c, r));
                    else if (_grid[c, r] == SlotSymbol.Shampoo) shampooCells.Add((c, r));
                }
            }

            int duckSpins = duckCells.Count >= 5 ? 12 : duckCells.Count == 4 ? 8 : duckCells.Count == 3 ? 5 : 0;
            int shampooSpins = shampooCells.Count >= 5 ? 12 : shampooCells.Count == 4 ? 8 : shampooCells.Count == 3 ? 5 : 0;
            res.ScatterBonusSpins = Math.Max(duckSpins, shampooSpins);

            if (duckCells.Count >= 3)
            {
                foreach (var cell in duckCells) res.BonusTriggerCells.Add(cell);
                int dMult = duckCells.Count >= 5 ? 40 : duckCells.Count == 4 ? 15 : 6;
                res.TotalPayoutCents += dMult * lineUnit * bonusMult;
            }
            if (shampooCells.Count >= 3)
            {
                foreach (var cell in shampooCells) res.BonusTriggerCells.Add(cell);
                var sSpec = Specs[SlotSymbol.Shampoo];
                int sMult = shampooCells.Count >= 5 ? sSpec.Pay5 : shampooCells.Count == 4 ? sSpec.Pay4 : sSpec.Pay3;
                res.TotalPayoutCents += sMult * lineUnit * bonusMult;
            }

            if (res.ProgressiveWon)
            {
                res.TotalPayoutCents = progressiveCents;
                res.ScatterBonusSpins = 0;
                res.BonusTriggerCells.Clear();
                res.WinningLines.RemoveAll(w => !w.IsProgressive);
            }

            return res;
        }

        private bool HasDuckOrWild()
        {
            for (int c = 0; c < 5; c++)
                for (int r = 0; r < 3; r++)
                    if (_grid[c, r] == SlotSymbol.Duck || _grid[c, r] == SlotSymbol.Wild) return true;
            return false;
        }

        private List<(int col, int row)> PickHotSwapSpots()
        {
            var list = new List<(int, int)>();
            for (int c = 0; c < 5; c++)
                for (int r = 0; r < 3; r++)
                    if (_grid[c, r] != SlotSymbol.Wild && _grid[c, r] != SlotSymbol.Shampoo)
                        list.Add((c, r));

            int count = Mathf.Min(UnityEngine.Random.Range(1, 3), list.Count);
            var picked = new List<(int, int)>(count);
            for (int i = 0; i < count; i++)
            {
                int idx = UnityEngine.Random.Range(0, list.Count);
                picked.Add(list[idx]);
                list.RemoveAt(idx);
            }
            return picked;
        }

        private SkillPromptData DetectOrBuildSkillPrompt(long wagerCents, int bonusMult)
        {
            foreach (var pl in AllPaylines)
            {
                var s0 = _grid[0, pl.Rows[0]];
                var s1 = _grid[1, pl.Rows[1]];
                SlotSymbol? target = null;
                if (s0 == s1 && s0 != SlotSymbol.Shampoo) target = s0;
                else if (s0 == SlotSymbol.Wild && s1 != SlotSymbol.Shampoo) target = s1;
                else if (s1 == SlotSymbol.Wild && s0 != SlotSymbol.Shampoo) target = s0;

                if (target.HasValue)
                {
                    long lineUnit = Math.Max(5L, wagerCents / 5L);
                    long pot = Math.Max(wagerCents * 2L, Specs[target.Value].Pay3 * lineUnit * bonusMult);
                    var choices = BuildCandidateChoices(target.Value);
                    return new SkillPromptData
                    {
                        TargetColumn = 2,
                        WinningRow = pl.Rows[2],
                        TargetSymbol = target.Value,
                        CandidateSymbols = choices,
                        PotentialWinCents = pot
                    };
                }
            }

            if (UnityEngine.Random.value < 0.25f)
            {
                return new SkillPromptData
                {
                    TargetColumn = 4,
                    WinningRow = 1,
                    TargetSymbol = SlotSymbol.Duck,
                    CandidateSymbols = BuildCandidateChoices(SlotSymbol.Duck),
                    PotentialWinCents = wagerCents * 6L
                };
            }
            return null;
        }

        private List<SlotSymbol> BuildCandidateChoices(SlotSymbol target)
        {
            var all = new List<SlotSymbol>((SlotSymbol[])Enum.GetValues(typeof(SlotSymbol)));
            all.Remove(target);
            all.Remove(SlotSymbol.Wild);
            var choices = new List<SlotSymbol> { target };
            for (int i = 0; i < 2 && all.Count > 0; i++)
            {
                int r = UnityEngine.Random.Range(0, all.Count);
                choices.Add(all[r]);
                all.RemoveAt(r);
            }
            for (int i = choices.Count - 1; i > 0; i--)
            {
                int j = UnityEngine.Random.Range(0, i + 1);
                var tmp = choices[i];
                choices[i] = choices[j];
                choices[j] = tmp;
            }
            return choices;
        }

        private void OnSkillChooseIndex(int idx)
        {
            if (_activeSkillPrompt == null || _activeSkillPrompt.IsSolved || IsAnyReelSpinning) return;
            var chosen = _activeSkillPrompt.CandidateSymbols[idx];
            if (chosen == _activeSkillPrompt.TargetSymbol)
            {
                SolveActiveSkill();
            }
            else
            {
                PlaySfx(_buttonTapClip, 0.65f);
                _activeSkillPrompt = null;
                _skillOverlayRoot.SetActive(false);
                SetStatus("MISSED SKILL PICK - SPIN AGAIN!");
            }
        }

        private void OnSkillNudge()
        {
            if (_activeSkillPrompt == null || _activeSkillPrompt.IsSolved || IsAnyReelSpinning) return;
            PlaySfx(_reelStopClip, 0.85f);
            SolveActiveSkill();
        }

        private void SolveActiveSkill()
        {
            var prompt = _activeSkillPrompt;
            prompt.IsSolved = true;
            _grid[prompt.TargetColumn, prompt.WinningRow] = prompt.TargetSymbol;
            _skillOverlayRoot.SetActive(false);
            ApplyGridVisuals();

            int mult = _bonusSpinsRemaining > 0 ? 2 : 1;
            var eval = EvaluateGrid(_wagerCents, _wagerCents >= MaxWagerCents, _progressiveCents, mult);
            if (eval.TotalPayoutCents < prompt.PotentialWinCents)
            {
                eval.TotalPayoutCents = prompt.PotentialWinCents;
            }
            _activeSkillPrompt = null;
            FinalizeOutcome(eval, true, false);
        }

        // ---------------------------------------------------------------------
        // AUDIO (Standard Unity AudioSource + Procedural AudioClips)
        // ---------------------------------------------------------------------

        private void SetupAudio()
        {
            _sfxSource = gameObject.AddComponent<AudioSource>();
            _sfxSource.playOnAwake = false;

            _loopSource = gameObject.AddComponent<AudioSource>();
            _loopSource.playOnAwake = false;
            _loopSource.loop = true;

            _spinStartClip = CreateToneSequenceClip("sfx_spin_start", new[] { (330f, 392f, 0.055f), (440f, 523f, 0.06f), (587f, 784f, 0.09f) }, 0.42f, 3.5f);
            _spinLoopClip = customSpinLoopClip != null ? customSpinLoopClip : CreateToneClip("sfx_spin_loop", 240f, 140f, 0.08f, 0.22f, 6.0f);
            _reelStopClip = customReelStopClip != null ? customReelStopClip : CreateToneClip("sfx_reel_stop", 210f, 78f, 0.085f, 0.50f, 4.5f);
            _winSmallClip = customWinClip != null ? customWinClip : CreateToneSequenceClip("sfx_win_small", new[] { (523.25f, 523.25f, 0.075f), (659.25f, 659.25f, 0.075f), (783.99f, 783.99f, 0.095f), (1046.5f, 1046.5f, 0.16f) }, 0.45f, 2.6f);
            _winBigClip = CreateToneSequenceClip("sfx_win_big", new[] { (523.25f, 523.25f, 0.08f), (659.25f, 659.25f, 0.08f), (783.99f, 783.99f, 0.08f), (1046.5f, 1046.5f, 0.13f), (783.99f, 783.99f, 0.075f), (1046.5f, 1046.5f, 0.26f) }, 0.50f, 2.2f);
            _bonusClip = customBonusClip != null ? customBonusClip : CreateToneSequenceClip("sfx_bonus", new[] { (880f, 1260f, 0.075f), (940f, 1380f, 0.085f), (587.33f, 587.33f, 0.07f), (739.99f, 739.99f, 0.07f), (880f, 880f, 0.08f), (1174.66f, 1174.66f, 0.24f) }, 0.48f, 2.3f);
            _bubblePopClip = CreateToneClip("sfx_bubble_pop", 360f, 1050f, 0.075f, 0.45f, 3.5f);
            _buttonTapClip = CreateToneClip("sfx_tap", 660f, 440f, 0.038f, 0.35f, 5.5f);
        }

        private void PlaySfx(AudioClip clip, float volume, float pitch = 1.0f)
        {
            if (!_soundEnabled || clip == null || _sfxSource == null) return;
            _sfxSource.pitch = pitch;
            _sfxSource.PlayOneShot(clip, volume);
        }

        private void StartSpinLoop()
        {
            if (!_soundEnabled || _loopSource == null || _spinLoopClip == null) return;
            _loopSource.clip = _spinLoopClip;
            _loopSource.volume = 0.35f;
            _loopSource.Play();
        }

        private void StopSpinLoop()
        {
            if (_loopSource != null && _loopSource.isPlaying) _loopSource.Stop();
        }

        private static AudioClip CreateToneClip(string name, float f0, float f1, float seconds, float vol, float decay)
        {
            const int sr = 22050;
            int n = Mathf.Max(1, Mathf.RoundToInt(sr * seconds));
            float[] samples = new float[n];
            float phase = 0f;
            for (int i = 0; i < n; i++)
            {
                float p = (float)i / n;
                float f = Mathf.Lerp(f0, f1, p);
                phase += 2f * Mathf.PI * f / sr;
                float env = Mathf.Exp(-decay * p) * Mathf.Min(1f, i / 60f);
                samples[i] = (Mathf.Sin(phase) + 0.25f * Mathf.Sin(2f * phase)) * env * vol;
            }
            var clip = AudioClip.Create(name, n, 1, sr, false);
            clip.SetData(samples, 0);
            return clip;
        }

        private static AudioClip CreateToneSequenceClip(string name, (float f0, float f1, float sec)[] notes, float vol, float decay)
        {
            const int sr = 22050;
            var list = new List<float>();
            foreach (var note in notes)
            {
                int n = Mathf.Max(1, Mathf.RoundToInt(sr * note.sec));
                float phase = 0f;
                for (int i = 0; i < n; i++)
                {
                    float p = (float)i / n;
                    float f = Mathf.Lerp(note.f0, note.f1, p);
                    phase += 2f * Mathf.PI * f / sr;
                    float env = Mathf.Exp(-decay * p) * Mathf.Min(1f, i / 60f);
                    list.Add((Mathf.Sin(phase) + 0.25f * Mathf.Sin(2f * phase)) * env * vol);
                }
            }
            var clip = AudioClip.Create(name, list.Count, 1, sr, false);
            clip.SetData(list.ToArray(), 0);
            return clip;
        }

        // ---------------------------------------------------------------------
        // PROCEDURAL UI CONSTRUCTION & VISUALS
        // ---------------------------------------------------------------------

        private void BuildCabinetUI()
        {
            if (FindFirstObjectByTypeCompat<EventSystem>() == null)
            {
                var esObj = new GameObject("EventSystem", typeof(EventSystem), typeof(StandaloneInputModule));
                esObj.transform.SetParent(transform, false);
            }

            var canvasObj = new GameObject("BathtimeBucksCanvas", typeof(Canvas), typeof(CanvasScaler), typeof(GraphicRaycaster));
            canvasObj.transform.SetParent(transform, false);
            var canvas = canvasObj.GetComponent<Canvas>();
            canvas.renderMode = RenderMode.ScreenSpaceOverlay;
            canvas.sortingOrder = 10;

            var scaler = canvasObj.GetComponent<CanvasScaler>();
            scaler.uiScaleMode = CanvasScaler.ScaleMode.ScaleWithScreenSize;
            scaler.referenceResolution = new Vector2(1080f, 1920f);
            scaler.matchWidthOrHeight = 0.5f;

            // Background Bathroom Tile Wall
            var bg = CreatePanel(canvasObj.transform, "Background", new Color(0.03f, 0.14f, 0.30f));
            StretchFull(bg.GetComponent<RectTransform>());

            // Safe Root Cabinet Container
            var rootObj = new GameObject("CabinetRoot", typeof(RectTransform));
            rootObj.transform.SetParent(bg.transform, false);
            var rootRect = rootObj.GetComponent<RectTransform>();
            StretchFull(rootRect, 20f, 20f, 36f, 24f);

            var rootLayout = rootObj.AddComponent<VerticalLayoutGroup>();
            rootLayout.spacing = 16f;
            rootLayout.childControlWidth = true;
            rootLayout.childControlHeight = true;
            rootLayout.childForceExpandWidth = true;
            rootLayout.childForceExpandHeight = false;

            // 1. Marquee Header
            BuildMarqueeHeader(rootObj.transform);

            // 2. 5x3 Reel Grid
            BuildReelGrid(rootObj.transform);

            // 3. Control Console
            BuildControlDeck(rootObj.transform);

            // 4. Grand Bonus / Scatter Celebration Overlay
            BuildCelebrationOverlay(canvasObj.transform);

            // 5. Modals (Prizes, Collect, Settings)
            BuildModals(canvasObj.transform);
        }

        private void BuildMarqueeHeader(Transform parent)
        {
            var marquee = CreatePanel(parent, "MarqueeHeader", new Color(0.06f, 0.36f, 0.68f));
            var le = marquee.AddComponent<LayoutElement>();
            le.preferredHeight = 320f;
            le.flexibleHeight = 0.22f;
            AddOutline(marquee, new Color(1f, 0.83f, 0f), 4f);

            // Yellow shower curtain strips on left & right
            var leftCurtain = CreatePanel(marquee.transform, "LeftCurtain", new Color(0.95f, 0.72f, 0.08f));
            var lcr = leftCurtain.GetComponent<RectTransform>();
            lcr.anchorMin = new Vector2(0f, 0f);
            lcr.anchorMax = new Vector2(0.09f, 1f);
            lcr.offsetMin = lcr.offsetMax = Vector2.zero;

            var rightCurtain = CreatePanel(marquee.transform, "RightCurtain", new Color(0.95f, 0.72f, 0.08f));
            var rcr = rightCurtain.GetComponent<RectTransform>();
            rcr.anchorMin = new Vector2(0.91f, 0f);
            rcr.anchorMax = new Vector2(1f, 1f);
            rcr.offsetMin = rcr.offsetMax = Vector2.zero;

            // Center Duck Mascot
            var duckObj = new GameObject("MarqueeDuck", typeof(Image));
            duckObj.transform.SetParent(marquee.transform, false);
            var duckImg = duckObj.GetComponent<Image>();
            duckImg.sprite = _sprites[SlotSymbol.Duck];
            duckImg.preserveAspect = true;
            var dr = duckObj.GetComponent<RectTransform>();
            dr.anchorMin = new Vector2(0.4f, 0.35f);
            dr.anchorMax = new Vector2(0.6f, 0.92f);
            dr.offsetMin = dr.offsetMax = Vector2.zero;

            // HOT SWAP badge (Top Left)
            var hotBadge = CreatePanel(marquee.transform, "HotSwapBadge", new Color(0.92f, 0.32f, 0.08f));
            var hbr = hotBadge.GetComponent<RectTransform>();
            hbr.anchorMin = new Vector2(0.11f, 0.74f);
            hbr.anchorMax = new Vector2(0.34f, 0.92f);
            hbr.offsetMin = hbr.offsetMax = Vector2.zero;
            AddOutline(hotBadge, new Color(1f, 0.83f, 0f), 2f);
            var hotTxt = CreateLabel(hotBadge.transform, "HOT SWAP", 26, new Color(1f, 0.88f, 0.2f), FontStyle.Bold);
            StretchFull(hotTxt.rectTransform);

            // BONUS SPINS badge (Below Hot Swap)
            _bonusSpinsBadge = CreatePanel(marquee.transform, "BonusSpinsBadge", new Color(0.13f, 0.77f, 0.37f));
            var bsr = _bonusSpinsBadge.GetComponent<RectTransform>();
            bsr.anchorMin = new Vector2(0.11f, 0.54f);
            bsr.anchorMax = new Vector2(0.38f, 0.71f);
            bsr.offsetMin = bsr.offsetMax = Vector2.zero;
            AddOutline(_bonusSpinsBadge, Color.white, 2f);
            _bonusSpinsBadgeText = CreateLabel(_bonusSpinsBadge.transform, "BONUS SPINS: 0", 22, Color.white, FontStyle.Bold);
            StretchFull(_bonusSpinsBadgeText.rectTransform);
            _bonusSpinsBadge.SetActive(false);

            // PROGRESSIVE TOP PRIZE plaque (Top Right)
            var progBox = CreatePanel(marquee.transform, "ProgressivePlaque", new Color(0.02f, 0.09f, 0.22f, 0.92f));
            var pbr = progBox.GetComponent<RectTransform>();
            pbr.anchorMin = new Vector2(0.62f, 0.58f);
            pbr.anchorMax = new Vector2(0.89f, 0.92f);
            pbr.offsetMin = pbr.offsetMax = Vector2.zero;
            AddOutline(progBox, new Color(0.11f, 0.71f, 0.88f), 2f);

            var progCol = progBox.AddComponent<VerticalLayoutGroup>();
            progCol.padding = new RectOffset(8, 8, 6, 6);
            progCol.childAlignment = TextAnchor.MiddleCenter;
            _progressiveTagText = CreateLabel(progBox.transform, "PROGRESSIVE ($2 MAX)", 18, new Color(0.73f, 0.90f, 0.99f), FontStyle.Bold);
            _progressiveText = CreateLabel(progBox.transform, "$2,537.59", 34, new Color(1f, 0.83f, 0f), FontStyle.Bold);

            // Big BATHTIME BUCKS Title
            var title = CreateLabel(marquee.transform, "BATHTIME BUCKS", 64, new Color(1f, 0.86f, 0.08f), FontStyle.Bold);
            var tr = title.rectTransform;
            tr.anchorMin = new Vector2(0.08f, 0.04f);
            tr.anchorMax = new Vector2(0.92f, 0.42f);
            tr.offsetMin = tr.offsetMax = Vector2.zero;
            AddOutline(title.gameObject, new Color(0.02f, 0.09f, 0.22f), 4f);
        }

        private void BuildReelGrid(Transform parent)
        {
            var outerFrame = CreatePanel(parent, "ReelFrame", new Color(0.82f, 0.45f, 0.04f));
            var le = outerFrame.AddComponent<LayoutElement>();
            le.flexibleHeight = 1f;
            le.minHeight = 520f;
            AddOutline(outerFrame, new Color(1f, 0.92f, 0.48f), 4f);

            var innerWell = CreatePanel(outerFrame.transform, "ReelWell", new Color(0.02f, 0.14f, 0.27f));
            StretchFull(innerWell.GetComponent<RectTransform>(), 24f, 24f, 18f, 18f);
            AddOutline(innerWell, new Color(0.06f, 0.09f, 0.16f), 3f);

            var columnsRow = new GameObject("ColumnsRow", typeof(RectTransform));
            columnsRow.transform.SetParent(innerWell.transform, false);
            StretchFull(columnsRow.GetComponent<RectTransform>(), 10f, 10f, 10f, 10f);

            var hlg = columnsRow.AddComponent<HorizontalLayoutGroup>();
            hlg.spacing = 10f;
            hlg.childControlWidth = true;
            hlg.childControlHeight = true;
            hlg.childForceExpandWidth = true;
            hlg.childForceExpandHeight = true;

            for (int col = 0; col < 5; col++)
            {
                var colPanel = CreatePanel(columnsRow.transform, "ReelCol_" + col, new Color(0.06f, 0.30f, 0.49f));
                colPanel.AddComponent<RectMask2D>();
                AddOutline(colPanel, new Color(0.22f, 0.74f, 0.97f, 0.35f), 2f);

                var colRect = colPanel.GetComponent<RectTransform>();
                int capturedCol = col;
                var tracker = colPanel.AddComponent<RectSizeTracker>();
                tracker.OnHeightChanged = h => _reelHeights[capturedCol] = h;

                // 3 Static Symbol Cells
                for (int row = 0; row < 3; row++)
                {
                    var cellObj = new GameObject($"Cell_{col}_{row}", typeof(RectTransform), typeof(Image));
                    cellObj.transform.SetParent(colPanel.transform, false);
                    var border = cellObj.GetComponent<Image>();
                    border.color = new Color(1f, 0.83f, 0f, 0.35f);
                    border.enabled = false;
                    _staticCellBorders[col, row] = border;

                    var cellRect = cellObj.GetComponent<RectTransform>();
                    cellRect.anchorMin = new Vector2(0f, 1f);
                    cellRect.anchorMax = new Vector2(1f, 1f);
                    cellRect.pivot = new Vector2(0.5f, 1f);
                    cellRect.sizeDelta = new Vector2(-8f, 160f);
                    _staticCellRects[col, row] = cellRect;

                    var symObj = new GameObject("Symbol", typeof(Image));
                    symObj.transform.SetParent(cellObj.transform, false);
                    var symImg = symObj.GetComponent<Image>();
                    symImg.preserveAspect = true;
                    StretchFull(symObj.GetComponent<RectTransform>(), 8f, 8f, 8f, 8f);
                    _staticCellImages[col, row] = symImg;

                    var bonusTag = CreatePanel(cellObj.transform, "BonusTag", new Color(0.13f, 0.77f, 0.37f));
                    var btr = bonusTag.GetComponent<RectTransform>();
                    btr.anchorMin = new Vector2(0.18f, 0.02f);
                    btr.anchorMax = new Vector2(0.82f, 0.22f);
                    btr.offsetMin = btr.offsetMax = Vector2.zero;
                    var btt = CreateLabel(bonusTag.transform, "BONUS", 16, Color.white, FontStyle.Bold);
                    StretchFull(btt.rectTransform);
                    bonusTag.SetActive(false);
                    _bonusCellTags[col, row] = bonusTag;
                }

                // 4 Spinning Strip Slots
                var spinImgs = new Image[4];
                var spinRects = new RectTransform[4];
                for (int s = 0; s < 4; s++)
                {
                    var slotObj = new GameObject($"SpinSlot_{col}_{s}", typeof(Image));
                    slotObj.transform.SetParent(colPanel.transform, false);
                    var img = slotObj.GetComponent<Image>();
                    img.preserveAspect = true;
                    img.color = new Color(1f, 1f, 1f, 0.92f);
                    var r = slotObj.GetComponent<RectTransform>();
                    r.anchorMin = new Vector2(0f, 1f);
                    r.anchorMax = new Vector2(1f, 1f);
                    r.pivot = new Vector2(0.5f, 1f);
                    r.sizeDelta = new Vector2(-16f, 150f);
                    slotObj.SetActive(false);
                    spinImgs[s] = img;
                    spinRects[s] = r;
                }
                _spinningSlotImages.Add(spinImgs);
                _spinningSlotRects.Add(spinRects);
            }

            // Skill Nudge / Choose Overlay (Anchored over target column)
            _skillOverlayRoot = CreatePanel(innerWell.transform, "SkillOverlay", new Color(0.02f, 0.10f, 0.23f, 0.86f));
            _skillOverlayRect = _skillOverlayRoot.GetComponent<RectTransform>();
            AddOutline(_skillOverlayRoot, new Color(1f, 0.83f, 0f), 3f);
            var skLayout = _skillOverlayRoot.AddComponent<VerticalLayoutGroup>();
            skLayout.padding = new RectOffset(8, 8, 10, 10);
            skLayout.spacing = 8f;
            skLayout.childAlignment = TextAnchor.MiddleCenter;
            skLayout.childControlWidth = true;
            skLayout.childControlHeight = true;

            CreateButton(_skillOverlayRoot.transform, "NUDGE UP", new Color(0.13f, 0.77f, 0.37f), Color.white, 22, OnSkillNudge);
            for (int i = 0; i < 3; i++)
            {
                int captured = i;
                var choiceBtn = CreatePanel(_skillOverlayRoot.transform, "Choice_" + i, new Color(0.06f, 0.20f, 0.38f));
                AddOutline(choiceBtn, new Color(1f, 0.83f, 0f), 2f);
                var btn = choiceBtn.AddComponent<Button>();
                btn.onClick.AddListener(() => OnSkillChooseIndex(captured));
                var iconObj = new GameObject("Icon", typeof(Image));
                iconObj.transform.SetParent(choiceBtn.transform, false);
                var img = iconObj.GetComponent<Image>();
                img.preserveAspect = true;
                StretchFull(iconObj.GetComponent<RectTransform>(), 8f, 8f, 8f, 8f);
                _skillChoiceImages[i] = img;
            }
            CreateButton(_skillOverlayRoot.transform, "NUDGE DN", new Color(0.13f, 0.77f, 0.37f), Color.white, 22, OnSkillNudge);
            _skillOverlayRoot.SetActive(false);

            // Instant Win Plaque
            _instantWinPlaque = CreatePanel(innerWell.transform, "InstantWinPlaque", new Color(0.03f, 0.16f, 0.31f, 0.95f));
            var iwr = _instantWinPlaque.GetComponent<RectTransform>();
            iwr.anchorMin = new Vector2(0.24f, 0.30f);
            iwr.anchorMax = new Vector2(0.76f, 0.70f);
            iwr.offsetMin = iwr.offsetMax = Vector2.zero;
            AddOutline(_instantWinPlaque, new Color(1f, 0.83f, 0f), 4f);
            var iwLayout = _instantWinPlaque.AddComponent<VerticalLayoutGroup>();
            iwLayout.padding = new RectOffset(16, 16, 12, 12);
            iwLayout.childAlignment = TextAnchor.MiddleCenter;
            CreateLabel(_instantWinPlaque.transform, "INSTANT WIN!", 44, new Color(1f, 0.83f, 0f), FontStyle.Bold);
            _instantWinAmountText = CreateLabel(_instantWinPlaque.transform, "$12.00", 54, Color.white, FontStyle.Bold);
            _instantWinPlaque.SetActive(false);
        }

        private void BuildControlDeck(Transform parent)
        {
            var deck = CreatePanel(parent, "ControlDeck", new Color(0.06f, 0.08f, 0.12f));
            var le = deck.AddComponent<LayoutElement>();
            le.preferredHeight = 360f;
            le.flexibleHeight = 0.24f;
            AddOutline(deck, new Color(0.20f, 0.25f, 0.33f), 3f);

            var vlg = deck.AddComponent<VerticalLayoutGroup>();
            vlg.padding = new RectOffset(14, 14, 12, 10);
            vlg.spacing = 10f;
            vlg.childControlWidth = true;
            vlg.childControlHeight = true;

            // Status LCD Strip
            var statusBox = CreatePanel(deck.transform, "StatusBox", Color.black);
            statusBox.AddComponent<LayoutElement>().preferredHeight = 48f;
            _statusBannerText = CreateLabel(statusBox.transform, "GOOD LUCK!", 26, Color.white, FontStyle.Bold);
            StretchFull(_statusBannerText.rectTransform);

            // Meters Row
            var metersRow = new GameObject("MetersRow", typeof(RectTransform));
            metersRow.transform.SetParent(deck.transform, false);
            metersRow.AddComponent<LayoutElement>().preferredHeight = 96f;
            var mh = metersRow.AddComponent<HorizontalLayoutGroup>();
            mh.spacing = 12f;
            mh.childControlWidth = true;
            mh.childControlHeight = true;
            mh.childForceExpandWidth = true;

            // Wager Stepper
            var wagerBox = CreatePanel(metersRow.transform, "WagerBox", new Color(0.07f, 0.10f, 0.17f));
            var wh = wagerBox.AddComponent<HorizontalLayoutGroup>();
            wh.padding = new RectOffset(8, 8, 8, 8);
            wh.spacing = 6f;
            wh.childControlWidth = true;
            wh.childControlHeight = true;
            CreateButton(wagerBox.transform, "<", new Color(0.86f, 0.15f, 0.15f), Color.white, 32, () => OnStepWager(-1)).AddComponent<LayoutElement>().preferredWidth = 70f;
            _wagerText = CreateLabel(wagerBox.transform, "$1.00", 30, Color.white, FontStyle.Bold);
            _wagerText.gameObject.AddComponent<LayoutElement>().flexibleWidth = 1f;
            CreateButton(wagerBox.transform, ">", new Color(0.86f, 0.15f, 0.15f), Color.white, 32, () => OnStepWager(1)).AddComponent<LayoutElement>().preferredWidth = 70f;

            // WIN & BALANCE LCD
            var lcdBox = CreatePanel(metersRow.transform, "LcdBox", Color.black);
            var lv = lcdBox.AddComponent<VerticalLayoutGroup>();
            lv.padding = new RectOffset(16, 16, 8, 8);
            lv.childControlWidth = true;
            lv.childControlHeight = true;
            _winText = CreateLabel(lcdBox.transform, "WIN: $0.00", 26, new Color(1f, 0.83f, 0f), FontStyle.Bold);
            _winText.alignment = TextAnchor.MiddleLeft;
            _balanceText = CreateLabel(lcdBox.transform, "BALANCE: $1,000.00", 28, new Color(0.11f, 0.71f, 0.88f), FontStyle.Bold);
            _balanceText.alignment = TextAnchor.MiddleLeft;

            // Buttons Row: COLLECT | PRIZES | SETTINGS | MAX | PLAY
            var btnRow = new GameObject("ButtonsRow", typeof(RectTransform));
            btnRow.transform.SetParent(deck.transform, false);
            btnRow.AddComponent<LayoutElement>().preferredHeight = 110f;
            var bh = btnRow.AddComponent<HorizontalLayoutGroup>();
            bh.spacing = 10f;
            bh.childControlWidth = true;
            bh.childControlHeight = true;
            bh.childForceExpandWidth = true;

            CreateButton(btnRow.transform, "COLLECT", new Color(0.08f, 0.50f, 0.24f), Color.white, 24, OpenCollectModal);
            CreateButton(btnRow.transform, "PRIZES", new Color(0.01f, 0.52f, 0.78f), Color.white, 24, OpenPrizeModal);
            CreateButton(btnRow.transform, "SET", new Color(0.28f, 0.33f, 0.41f), Color.white, 24, OpenSettingsModal).AddComponent<LayoutElement>().preferredWidth = 110f;
            CreateButton(btnRow.transform, "MAX", new Color(0.98f, 0.80f, 0.08f), new Color(0.02f, 0.09f, 0.22f), 28, OnMaxClick);

            var playBtnObj = CreateButton(btnRow.transform, "PLAY", new Color(0.13f, 0.77f, 0.37f), Color.white, 34, OnPlayClick);
            playBtnObj.AddComponent<LayoutElement>().flexibleWidth = 1.4f;
            _playButtonBg = playBtnObj.GetComponent<Image>();
            _playButtonText = playBtnObj.GetComponentInChildren<Text>();

            var notice = CreateLabel(deck.transform, "FOR AMUSEMENT ONLY - VIRTUAL CREDITS - NO REAL MONEY", 17, new Color(0.39f, 0.45f, 0.55f), FontStyle.Normal);
            notice.gameObject.AddComponent<LayoutElement>().preferredHeight = 26f;
        }

        private void BuildCelebrationOverlay(Transform parent)
        {
            _celebrationOverlayRoot = CreatePanel(parent, "BonusCelebrationOverlay", new Color(0.02f, 0.09f, 0.22f, 0.82f));
            StretchFull(_celebrationOverlayRoot.GetComponent<RectTransform>());
            var dismissBtn = _celebrationOverlayRoot.AddComponent<Button>();
            dismissBtn.transition = Selectable.Transition.None;
            dismissBtn.onClick.AddListener(DismissBonusCelebration);

            // Rotating Sunburst Rays
            var raysObj = new GameObject("SunburstRays", typeof(RectTransform));
            raysObj.transform.SetParent(_celebrationOverlayRoot.transform, false);
            _celebrationRaysRect = raysObj.GetComponent<RectTransform>();
            _celebrationRaysRect.sizeDelta = new Vector2(1400f, 1400f);
            for (int i = 0; i < 12; i++)
            {
                var ray = CreatePanel(raysObj.transform, "Ray_" + i, new Color(1f, 0.83f, 0f, 0.16f));
                var rr = ray.GetComponent<RectTransform>();
                rr.sizeDelta = new Vector2(90f, 1400f);
                rr.localRotation = Quaternion.Euler(0f, 0f, i * 30f);
                ray.GetComponent<Image>().raycastTarget = false;
            }

            // Bursting Coins & Bubbles Particles
            for (int i = 0; i < 36; i++)
            {
                bool isCoin = i % 2 == 0;
                var pObj = new GameObject("Particle_" + i, typeof(Image));
                pObj.transform.SetParent(_celebrationOverlayRoot.transform, false);
                var pImg = pObj.GetComponent<Image>();
                pImg.sprite = _circleSprite;
                pImg.color = isCoin ? new Color(1f, 0.83f, 0f, 0.9f) : new Color(0.45f, 0.90f, 1f, 0.7f);
                pImg.raycastTarget = false;
                var prt = pObj.GetComponent<RectTransform>();
                float sz = UnityEngine.Random.Range(18f, 44f);
                prt.sizeDelta = new Vector2(sz, sz);
                _celebrationParticles.Add(prt);
                float angle = (i / 36f) * Mathf.PI * 2f;
                _particleDirs.Add(new Vector2(Mathf.Cos(angle), Mathf.Sin(angle)));
                _particleSpeeds.Add(UnityEngine.Random.Range(160f, 420f));
            }

            // Center Celebration Card
            var card = CreatePanel(_celebrationOverlayRoot.transform, "CelebrationCard", new Color(0.04f, 0.24f, 0.38f));
            var cr = card.GetComponent<RectTransform>();
            cr.sizeDelta = new Vector2(820f, 520f);
            AddOutline(card, new Color(1f, 0.83f, 0f), 6f);

            var vlg = card.AddComponent<VerticalLayoutGroup>();
            vlg.padding = new RectOffset(28, 28, 28, 24);
            vlg.spacing = 14f;
            vlg.childAlignment = TextAnchor.MiddleCenter;
            vlg.childControlWidth = true;
            vlg.childControlHeight = true;

            _celebrationTitleText = CreateLabel(card.transform, "DUCKY BONUS!", 60, new Color(1f, 0.86f, 0.08f), FontStyle.Bold);
            _celebrationSubText = CreateLabel(card.transform, "3 RUBBER DUCKS LANDED!", 28, new Color(0.73f, 0.90f, 0.99f), FontStyle.Bold);

            var spinsBanner = CreatePanel(card.transform, "SpinsBanner", new Color(0.13f, 0.77f, 0.37f));
            spinsBanner.AddComponent<LayoutElement>().preferredHeight = 100f;
            AddOutline(spinsBanner, Color.white, 3f);
            _celebrationSpinsText = CreateLabel(spinsBanner.transform, "+5 FREE BONUS SPINS! (2X WINS)", 36, Color.white, FontStyle.Bold);
            StretchFull(_celebrationSpinsText.rectTransform);

            _celebrationAmountText = CreateLabel(card.transform, "$3.00", 62, Color.white, FontStyle.Bold);
            CreateLabel(card.transform, "TAP ANYWHERE TO CONTINUE", 22, new Color(1f, 1f, 1f, 0.78f), FontStyle.Normal);

            _celebrationOverlayRoot.SetActive(false);
        }

        private void ShowGrandCelebration(int spins, long payoutCents, bool isProgressive, int duckCount)
        {
            _celebrationTitleText.text = isProgressive ? "PROGRESSIVE!" : (duckCount >= 3 ? "DUCKY BONUS!" : "SCATTER BONUS!");
            _celebrationSubText.text = duckCount >= 3 ? duckCount + " RUBBER DUCKS LANDED!" : "BONUS TRIGGERED!";
            _celebrationSpinsText.gameObject.transform.parent.gameObject.SetActive(spins > 0);
            _celebrationSpinsText.text = "+" + spins + " FREE BONUS SPINS! (2X WINS)";
            _celebrationAmountText.text = FormatCents(payoutCents);
            foreach (var p in _celebrationParticles) p.anchoredPosition = Vector2.zero;
            _celebrationOverlayRoot.SetActive(true);

            if (_celebrationCoroutine != null) StopCoroutine(_celebrationCoroutine);
            _celebrationCoroutine = StartCoroutine(AutoDismissCelebration());
        }

        private IEnumerator AutoDismissCelebration()
        {
            yield return new WaitForSeconds(2.6f);
            DismissBonusCelebration();
        }

        public void DismissBonusCelebration()
        {
            if (!_celebrationOverlayRoot.activeSelf) return;
            if (_celebrationCoroutine != null) StopCoroutine(_celebrationCoroutine);
            _celebrationOverlayRoot.SetActive(false);
            if (_bonusSpinsRemaining > 0)
            {
                StartCoroutine(AutoTriggerNextBonusSpin(0.45f));
            }
        }

        private void BuildModals(Transform parent)
        {
            // Prize Modal
            _prizeModalRoot = BuildSimpleModal(parent, "PrizeModal", "PRIZE CHART & RULES", out _prizeModalBodyText, () => _prizeModalRoot.SetActive(false));
            // Collect Modal
            _collectModalRoot = BuildSimpleModal(parent, "CollectModal", "AMUSEMENT TICKET", out _collectModalBodyText, () => _collectModalRoot.SetActive(false), "REFILL +$1,000", () =>
            {
                _balanceCents += 100000L;
                PlaySfx(_winSmallClip, 0.85f);
                _collectModalRoot.SetActive(false);
                SetStatus("ADDED $1,000.00 VIRTUAL CREDITS!");
                SavePrefs();
                RefreshAllUI();
            });
            // Settings Modal
            _settingsModalRoot = CreatePanel(parent, "SettingsModal", new Color(0.02f, 0.09f, 0.22f, 0.85f));
            StretchFull(_settingsModalRoot.GetComponent<RectTransform>());
            var card = CreatePanel(_settingsModalRoot.transform, "Card", new Color(0.05f, 0.22f, 0.45f));
            card.GetComponent<RectTransform>().sizeDelta = new Vector2(720f, 520f);
            AddOutline(card, new Color(0.11f, 0.71f, 0.88f), 4f);
            var v = card.AddComponent<VerticalLayoutGroup>();
            v.padding = new RectOffset(28, 28, 24, 24);
            v.spacing = 18f;
            v.childControlWidth = true;
            v.childControlHeight = true;
            CreateLabel(card.transform, "CABINET SETTINGS", 42, new Color(1f, 0.83f, 0f), FontStyle.Bold);
            var sndBtn = CreateButton(card.transform, "SOUND FX: ON", new Color(0.06f, 0.20f, 0.38f), Color.white, 28, () =>
            {
                _soundEnabled = !_soundEnabled;
                if (!_soundEnabled) StopSpinLoop();
                SavePrefs();
                RefreshSettingsLabels();
            });
            _soundToggleLabel = sndBtn.GetComponentInChildren<Text>();
            var sklBtn = CreateButton(card.transform, "SKILL NUDGE MODE: ON", new Color(0.06f, 0.20f, 0.38f), Color.white, 28, () =>
            {
                _skillModeEnabled = !_skillModeEnabled;
                SavePrefs();
                RefreshSettingsLabels();
            });
            _skillToggleLabel = sklBtn.GetComponentInChildren<Text>();
            CreateButton(card.transform, "DONE", new Color(0.13f, 0.77f, 0.37f), Color.white, 30, () => _settingsModalRoot.SetActive(false));
            _settingsModalRoot.SetActive(false);
        }

        private GameObject BuildSimpleModal(Transform parent, string name, string title, out Text bodyText, Action onClose, string extraBtnLabel = null, Action onExtra = null)
        {
            var overlay = CreatePanel(parent, name, new Color(0.02f, 0.09f, 0.22f, 0.85f));
            StretchFull(overlay.GetComponent<RectTransform>());
            var card = CreatePanel(overlay.transform, "Card", new Color(0.05f, 0.22f, 0.45f));
            card.GetComponent<RectTransform>().sizeDelta = new Vector2(820f, 820f);
            AddOutline(card, new Color(1f, 0.83f, 0f), 4f);
            var v = card.AddComponent<VerticalLayoutGroup>();
            v.padding = new RectOffset(28, 28, 24, 24);
            v.spacing = 14f;
            v.childControlWidth = true;
            v.childControlHeight = true;
            CreateLabel(card.transform, title, 40, new Color(1f, 0.83f, 0f), FontStyle.Bold).gameObject.AddComponent<LayoutElement>().preferredHeight = 64f;
            bodyText = CreateLabel(card.transform, "", 24, Color.white, FontStyle.Normal);
            bodyText.alignment = TextAnchor.UpperLeft;
            bodyText.gameObject.AddComponent<LayoutElement>().flexibleHeight = 1f;
            if (!string.IsNullOrEmpty(extraBtnLabel) && onExtra != null)
            {
                CreateButton(card.transform, extraBtnLabel, new Color(0.13f, 0.77f, 0.37f), Color.white, 28, onExtra).AddComponent<LayoutElement>().preferredHeight = 84f;
            }
            CreateButton(card.transform, "BACK TO GAME", new Color(0.11f, 0.71f, 0.88f), new Color(0.02f, 0.09f, 0.22f), 28, onClose).AddComponent<LayoutElement>().preferredHeight = 84f;
            overlay.SetActive(false);
            return overlay;
        }

        private void OpenPrizeModal()
        {
            PlaySfx(_buttonTapClip, 0.65f);
            long lineUnit = Math.Max(5L, _wagerCents / 5L);
            _prizeModalBodyText.text =
                "WAGERS: $0.25 / $0.50 / $1.00 / $2.00* (9 Paylines)\n" +
                "* $2.00 Max Play qualifies for Progressive Top Prize!\n\n" +
                "PAYOUTS AT " + FormatCents(_wagerCents) + ":\n" +
                "• WILD BUBBLE: 3x " + FormatCents(25 * lineUnit) + " | 5x " + FormatCents(500 * lineUnit) + "\n" +
                "• RUBBER DUCK: 3x " + FormatCents(15 * lineUnit) + " | 5x " + FormatCents(250 * lineUnit) + "\n" +
                "• TOY BOAT:    3x " + FormatCents(10 * lineUnit) + " | 5x " + FormatCents(150 * lineUnit) + "\n" +
                "• SOAP BAR:    3x " + FormatCents(8 * lineUnit) + "  | 5x " + FormatCents(100 * lineUnit) + "\n" +
                "• 3X/2X/1X BAR: Mixed BARs pay 2x / 5x / 15x!\n\n" +
                "SPECIAL FEATURES:\n" +
                "• DUCKY BONUS: 3, 4, or 5+ Rubber Ducks anywhere on the reels award 5, 8, or 12 Free Bonus Spins (2x wins)!\n" +
                "• HOT SWAP: Ducks/Wilds can bubble-swap adjacent tiles into WILDs.\n" +
                "• SKILL NUDGE: Pick the winning icon or Nudge on near-misses!";
            _prizeModalRoot.SetActive(true);
        }

        private void OpenCollectModal()
        {
            PlaySfx(_buttonTapClip, 0.65f);
            _collectModalBodyText.text =
                "BATHTIME BUCKS - VIRTUAL VOUCHER\n\n" +
                "CURRENT BALANCE:  " + FormatCents(_balanceCents) + "\n" +
                "TOTAL SPINS:      " + _totalSpins + "\n" +
                "TOTAL WON:        " + FormatCents(_totalWonCents) + "\n" +
                "BIGGEST WIN:      " + FormatCents(_biggestWinCents) + "\n\n" +
                "For Amusement Only - Virtual Credits Have No Cash Value.";
            _collectModalRoot.SetActive(true);
        }

        private void OpenSettingsModal()
        {
            PlaySfx(_buttonTapClip, 0.65f);
            RefreshSettingsLabels();
            _settingsModalRoot.SetActive(true);
        }

        private void RefreshSettingsLabels()
        {
            _soundToggleLabel.text = "SOUND FX: " + (_soundEnabled ? "ON" : "OFF");
            _skillToggleLabel.text = "SKILL NUDGE MODE: " + (_skillModeEnabled ? "ON" : "OFF");
        }

        private void ShowSkillOverlay(SkillPromptData prompt)
        {
            float colW = 1f / 5f;
            _skillOverlayRect.anchorMin = new Vector2(prompt.TargetColumn * colW, 0f);
            _skillOverlayRect.anchorMax = new Vector2((prompt.TargetColumn + 1) * colW, 1f);
            _skillOverlayRect.offsetMin = new Vector2(6f, 6f);
            _skillOverlayRect.offsetMax = new Vector2(-6f, -6f);
            for (int i = 0; i < 3; i++)
            {
                _skillChoiceImages[i].sprite = _sprites[prompt.CandidateSymbols[i]];
            }
            _skillOverlayRoot.SetActive(true);
        }

        private void ApplyGridVisuals()
        {
            var winCells = new HashSet<(int, int)>();
            if (_lastEval != null)
            {
                foreach (var wl in _lastEval.WinningLines)
                    for (int c = 0; c < wl.MatchCount; c++)
                        winCells.Add((c, wl.Payline.Rows[c]));
            }

            for (int c = 0; c < 5; c++)
            {
                for (int r = 0; r < 3; r++)
                {
                    _staticCellImages[c, r].sprite = _sprites[_grid[c, r]];
                    bool isWin = !_reelSpinning[c] && winCells.Contains((c, r));
                    bool isBonus = !_reelSpinning[c] && _lastEval != null && _lastEval.BonusTriggerCells.Contains((c, r));
                    _staticCellBorders[c, r].enabled = isWin || isBonus;
                    _staticCellBorders[c, r].color = isBonus
                        ? new Color(0.13f, 0.77f, 0.37f, 0.45f)
                        : new Color(1f, 0.83f, 0f, 0.35f);
                    _bonusCellTags[c, r].SetActive(isBonus);
                }
            }
        }

        private void RefreshAllUI()
        {
            bool progActive = _wagerCents >= MaxWagerCents;
            _progressiveTagText.text = progActive ? "PROGRESSIVE ACTIVE" : "PROGRESSIVE ($2 MAX)";
            _progressiveTagText.color = progActive ? new Color(0.13f, 0.77f, 0.37f) : new Color(0.73f, 0.90f, 0.99f);
            _progressiveText.text = FormatCents(_progressiveCents);

            _bonusSpinsBadge.SetActive(_bonusSpinsRemaining > 0);
            _bonusSpinsBadgeText.text = "BONUS SPINS: " + _bonusSpinsRemaining;

            _wagerText.text = FormatCents(_wagerCents);
            _winText.text = "WIN: " + FormatCents(_winCents);
            _balanceText.text = "BALANCE: " + FormatCents(_balanceCents);

            bool spinning = IsAnyReelSpinning;
            _playButtonText.text = spinning ? "STOP" : "PLAY";
            _playButtonBg.color = spinning ? new Color(0.86f, 0.15f, 0.15f) : new Color(0.13f, 0.77f, 0.37f);

            ApplyGridVisuals();
        }

        private void SetStatus(string text)
        {
            if (_statusBannerText != null) _statusBannerText.text = text;
        }

        // ---------------------------------------------------------------------
        // PROCEDURAL SYMBOL SPRITES (Zero External Asset Dependencies)
        // ---------------------------------------------------------------------

        private void GenerateProceduralSprites()
        {
            _circleSprite = MakeCircleSprite(64, Color.white);
            foreach (SlotSymbol sym in Enum.GetValues(typeof(SlotSymbol)))
            {
                int idx = (int)sym;
                if (customSymbolSprites != null && idx < customSymbolSprites.Length && customSymbolSprites[idx] != null)
                {
                    _sprites[sym] = customSymbolSprites[idx];
                }
                else
                {
                    _sprites[sym] = CreateSymbolSprite(sym);
                }
            }
        }

        private static Sprite MakeCircleSprite(int sz, Color fill)
        {
            var tex = new Texture2D(sz, sz, TextureFormat.RGBA32, false);
            float r = sz * 0.48f;
            Vector2 c = new Vector2(sz * 0.5f, sz * 0.5f);
            for (int y = 0; y < sz; y++)
            {
                for (int x = 0; x < sz; x++)
                {
                    float d = Vector2.Distance(new Vector2(x, y), c);
                    tex.SetPixel(x, y, d <= r ? fill : Color.clear);
                }
            }
            tex.Apply();
            return Sprite.Create(tex, new Rect(0, 0, sz, sz), new Vector2(0.5f, 0.5f));
        }

        private static Sprite CreateSymbolSprite(SlotSymbol sym)
        {
            const int sz = 96;
            var tex = new Texture2D(sz, sz, TextureFormat.RGBA32, false);
            var clear = new Color(0f, 0f, 0f, 0f);
            for (int y = 0; y < sz; y++)
                for (int x = 0; x < sz; x++)
                    tex.SetPixel(x, y, clear);

            Color accent = Specs[sym].AccentColor;
            switch (sym)
            {
                case SlotSymbol.Duck:
                    DrawDisc(tex, 52, 42, 24, new Color(1f, 0.84f, 0f)); // Body
                    DrawDisc(tex, 42, 66, 17, new Color(1f, 0.88f, 0.1f)); // Head
                    DrawRect(tex, 18, 58, 16, 10, new Color(1f, 0.34f, 0.13f)); // Bill
                    DrawDisc(tex, 36, 70, 3, Color.black); // Eye
                    break;
                case SlotSymbol.Wild:
                    DrawDisc(tex, 48, 48, 38, new Color(0.15f, 0.72f, 0.94f));
                    DrawDisc(tex, 48, 48, 28, new Color(1f, 0.84f, 0f));
                    DrawDisc(tex, 34, 66, 7, Color.white);
                    break;
                case SlotSymbol.TripleBar:
                    DrawBar(tex, 64, new Color(0.08f, 0.50f, 0.24f));
                    DrawBar(tex, 42, new Color(0.08f, 0.50f, 0.24f));
                    DrawBar(tex, 20, new Color(0.08f, 0.50f, 0.24f));
                    break;
                case SlotSymbol.DoubleBar:
                    DrawBar(tex, 54, new Color(0.01f, 0.52f, 0.78f));
                    DrawBar(tex, 28, new Color(0.01f, 0.52f, 0.78f));
                    break;
                case SlotSymbol.SingleBar:
                    DrawBar(tex, 40, new Color(0.85f, 0.47f, 0.02f));
                    break;
                case SlotSymbol.Cherry:
                    DrawDisc(tex, 32, 30, 14, new Color(0.86f, 0.12f, 0.12f));
                    DrawDisc(tex, 64, 28, 14, new Color(0.86f, 0.12f, 0.12f));
                    DrawRect(tex, 46, 42, 5, 34, new Color(0.13f, 0.77f, 0.37f));
                    break;
                default:
                    DrawDisc(tex, 48, 48, 32, accent);
                    DrawDisc(tex, 38, 60, 10, new Color(1f, 1f, 1f, 0.55f));
                    break;
            }
            tex.Apply();
            return Sprite.Create(tex, new Rect(0, 0, sz, sz), new Vector2(0.5f, 0.5f));
        }

        private static void DrawDisc(Texture2D tex, int cx, int cy, int r, Color c)
        {
            for (int y = Mathf.Max(0, cy - r); y <= Mathf.Min(tex.height - 1, cy + r); y++)
            {
                for (int x = Mathf.Max(0, cx - r); x <= Mathf.Min(tex.width - 1, cx + r); x++)
                {
                    if ((x - cx) * (x - cx) + (y - cy) * (y - cy) <= r * r) tex.SetPixel(x, y, c);
                }
            }
        }

        private static void DrawRect(Texture2D tex, int x0, int y0, int w, int h, Color c)
        {
            for (int y = Mathf.Max(0, y0); y < Mathf.Min(tex.height, y0 + h); y++)
                for (int x = Mathf.Max(0, x0); x < Mathf.Min(tex.width, x0 + w); x++)
                    tex.SetPixel(x, y, c);
        }

        private static void DrawBar(Texture2D tex, int y0, Color c)
        {
            DrawRect(tex, 10, y0 - 2, 76, 18, Color.white);
            DrawRect(tex, 13, y0, 70, 14, c);
        }

        // ---------------------------------------------------------------------
        // PERSISTENCE & UI HELPERS
        // ---------------------------------------------------------------------

        private void LoadPrefs()
        {
            if (PlayerPrefs.HasKey("bb_balance"))
            {
                long.TryParse(PlayerPrefs.GetString("bb_balance", "100000"), out _balanceCents);
                long.TryParse(PlayerPrefs.GetString("bb_wager", "100"), out _wagerCents);
                long.TryParse(PlayerPrefs.GetString("bb_prog", "253759"), out _progressiveCents);
                _totalSpins = PlayerPrefs.GetInt("bb_spins", 0);
                _soundEnabled = PlayerPrefs.GetInt("bb_sound", 1) == 1;
                _skillModeEnabled = PlayerPrefs.GetInt("bb_skill", 1) == 1;
            }
        }

        private void SavePrefs()
        {
            PlayerPrefs.SetString("bb_balance", _balanceCents.ToString());
            PlayerPrefs.SetString("bb_wager", _wagerCents.ToString());
            PlayerPrefs.SetString("bb_prog", _progressiveCents.ToString());
            PlayerPrefs.SetInt("bb_spins", _totalSpins);
            PlayerPrefs.SetInt("bb_sound", _soundEnabled ? 1 : 0);
            PlayerPrefs.SetInt("bb_skill", _skillModeEnabled ? 1 : 0);
            PlayerPrefs.Save();
        }

        private static string FormatCents(long cents)
        {
            bool neg = cents < 0;
            long abs = Math.Abs(cents);
            return (neg ? "-$" : "$") + (abs / 100L).ToString("N0") + "." + (abs % 100L).ToString("00");
        }

        private static int Mod(int a, int n)
        {
            int r = a % n;
            return r < 0 ? r + n : r;
        }

        private static Font GetBuiltinFont()
        {
            Font f = Resources.GetBuiltinResource<Font>("LegacyRuntime.ttf");
            if (f == null) f = Resources.GetBuiltinResource<Font>("Arial.ttf");
            return f;
        }

        private static T FindFirstObjectByTypeCompat<T>() where T : UnityEngine.Object
        {
#if UNITY_2023_1_OR_NEWER
            return FindFirstObjectByType<T>();
#else
            return FindObjectOfType<T>();
#endif
        }

        private static GameObject CreatePanel(Transform parent, string name, Color color)
        {
            var go = new GameObject(name, typeof(RectTransform), typeof(Image));
            go.transform.SetParent(parent, false);
            go.GetComponent<Image>().color = color;
            return go;
        }

        private Text CreateLabel(Transform parent, string text, int size, Color color, FontStyle style)
        {
            var go = new GameObject("Label", typeof(RectTransform), typeof(Text));
            go.transform.SetParent(parent, false);
            var t = go.GetComponent<Text>();
            t.font = _font;
            t.text = text;
            t.fontSize = size;
            t.color = color;
            t.fontStyle = style;
            t.alignment = TextAnchor.MiddleCenter;
            t.resizeTextForBestFit = true;
            t.resizeTextMinSize = 10;
            t.resizeTextMaxSize = size;
            return t;
        }

        private GameObject CreateButton(Transform parent, string label, Color bg, Color fg, int fontSize, Action onClick)
        {
            var go = CreatePanel(parent, "Btn_" + label, bg);
            AddOutline(go, new Color(1f, 1f, 1f, 0.7f), 2f);
            var btn = go.AddComponent<Button>();
            btn.onClick.AddListener(() => onClick());
            var txt = CreateLabel(go.transform, label, fontSize, fg, FontStyle.Bold);
            StretchFull(txt.rectTransform, 6f, 6f, 4f, 4f);
            return go;
        }

        private static void AddOutline(GameObject go, Color color, float dist)
        {
            var ol = go.AddComponent<Outline>();
            ol.effectColor = color;
            ol.effectDistance = new Vector2(dist, -dist);
        }

        private static void StretchFull(RectTransform rt, float left = 0f, float right = 0f, float top = 0f, float bottom = 0f)
        {
            rt.anchorMin = Vector2.zero;
            rt.anchorMax = Vector2.one;
            rt.offsetMin = new Vector2(left, bottom);
            rt.offsetMax = new Vector2(-right, -top);
        }

        private sealed class RectSizeTracker : MonoBehaviour
        {
            public Action<float> OnHeightChanged;
            private float _lastH = -1f;

            private void Update()
            {
                float h = ((RectTransform)transform).rect.height;
                if (Mathf.Abs(h - _lastH) > 0.5f)
                {
                    _lastH = h;
                    OnHeightChanged?.Invoke(h);
                }
            }
        }
    }
}
