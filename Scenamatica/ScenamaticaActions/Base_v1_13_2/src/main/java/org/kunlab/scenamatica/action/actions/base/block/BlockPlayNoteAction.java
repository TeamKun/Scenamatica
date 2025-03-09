package org.kunlab.scenamatica.action.actions.base.block;

import org.bukkit.Bukkit;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.event.Event;
import org.bukkit.event.block.NotePlayEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.world.NMSWorldServer;
import org.kunlab.scenamatica.structures.StructureMappers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Action("block_play_note")
@ActionDoc(
        name = "音ブロックの再生",
        description = "音ブロックを指定した音で再生します。",
        events = NotePlayEvent.class,

        executable = "指定された音ブロックを再生します。",
        requireable = "音ブロックが再生されることを期待します。",

        outputs = {
                @OutputDoc(
                        name = BlockPlayNoteAction.OUT_INSTRUMENT,
                        description = "再生された音の種類です。",
                        type = Instrument.class
                ),
                @OutputDoc(
                        name = BlockPlayNoteAction.OUT_NOTE,
                        description = "再生された音です。",
                        type = BlockPlayNoteAction.NoteInput.class
                )
        }
)
public class BlockPlayNoteAction extends AbstractBlockAction
        implements Executable, Expectable
{
    public static final String OUT_INSTRUMENT = "instrument";
    public static final String OUT_NOTE = "note";

    @InputDoc(
            name = "instrument",
            description = "再生する音の種類です。",
            type = Instrument.class,

            admonitions = {
                    @Admonition(
                            type = AdmonitionType.WARNING,
                            on = ActionMethod.EXECUTE,
                            content = "この入力を指定すると, 音の再生前に, 音ブロックの関連する属性を変更します。"
                    )
            }
    )
    public static final InputToken<Instrument> IN_INSTRUMENT = ofEnumInput(
            "instrument",
            Instrument.class
    );
    @InputDoc(
            name = "note",
            description = "再生する音の高さです。",
            type = NoteInput.class,

            admonitions = {
                    @Admonition(
                            type = AdmonitionType.WARNING,
                            on = ActionMethod.EXECUTE,
                            content = "この入力を指定すると, 音の再生前に, 音ブロックの関連する属性を変更します。"
                    )
            }
    )
    public static final InputToken<NoteInput> IN_NOTE = ofInput(
            "note",
            NoteInput.class,
            ofTraverser(
                    Number.class,
                    (ser, num) -> {
                        int value = num.intValue();
                        return new NoteInput(value);
                    }
            ),
            ofTraverser(
                    StructuredYamlNode.class,
                    NoteInput::deserialize
            )
    );

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Block block = super.getBlockLocationWithWorld(ctxt.input(IN_BLOCK), ctxt).getBlock();
        NoteBlock noteBlock = (NoteBlock) block.getBlockData();

        Instrument instrument = noteBlock.getInstrument();
        Note note = noteBlock.getNote();
        if (ctxt.hasInput(IN_INSTRUMENT))
            noteBlock.setInstrument(instrument = ctxt.input(IN_INSTRUMENT));
        if (ctxt.hasInput(IN_NOTE))
        {
            NoteInput noteInput = ctxt.input(IN_NOTE);
            note = noteInput.getNote();
            noteBlock.setNote(note);
        }

        block.setBlockData(noteBlock);
        this.makeOutputs(ctxt, instrument, note);

        // 音を再生するメソッドはないので, イベントの発火 => 音の再生をシミュレートする。
        NotePlayEvent event = new NotePlayEvent(block, instrument, note);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            this.simulateNoteBlockPlay(block, instrument, note);
    }

    @SuppressWarnings("deprecation")
    private void simulateNoteBlockPlay(Block block, Instrument instrument, Note note)
    {
        NMSWorldServer nmsWorld = NMSProvider.getProvider().wrap(block.getWorld());
        nmsWorld.playBlockAction(block, instrument.getType(), note.getId());
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedBlockEvent(ctxt, event))
            return false;

        assert event instanceof NotePlayEvent;
        NotePlayEvent notePlayEvent = (NotePlayEvent) event;

        Instrument inst = notePlayEvent.getInstrument();
        Note note = notePlayEvent.getNote();

        boolean result = ctxt.ifHasInput(IN_INSTRUMENT, in -> in == inst)
                && ctxt.ifHasInput(IN_NOTE, in -> in.isAdequate(note, false));
        if (result)
            this.makeOutputs(ctxt, inst, note);

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, Instrument instrument, Note note)
    {
        ctxt.output(OUT_INSTRUMENT, instrument);
        ctxt.output(OUT_NOTE, NoteInput.fromNote(note));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(NotePlayEvent.class);
    }

    @Override
    public InputBoard getInputBoard(@NotNull ScenarioType type)
    {
        return super.getInputBoard(type)
                .registerAll(IN_INSTRUMENT, IN_NOTE);
    }

    @TypeDoc(
            name = "音",
            description = "音の種類とピッチを指定します。",
            mappingOf = Note.class,
            properties = {
                    @TypeProperty(
                            name = NoteInput.TONE,
                            description = "C, D, E, F, G, A, B などの, 音名です。",
                            type = Note.Tone.class
                    ),
                    @TypeProperty(
                            name = NoteInput.OCTAVE,
                            description = "音のオクターブです。",
                            type = int.class,
                            defaultValue = "0",
                            min = 0,
                            max = 2
                    ),
                    @TypeProperty(
                            name = NoteInput.SHARP,
                            description = "半音上がっているかどうかです。",
                            type = boolean.class,
                            defaultValue = "false"
                    ),
                    @TypeProperty(
                            name = NoteInput.FLAT,
                            description = "半音下がっているかどうかです。",
                            type = boolean.class,
                            defaultValue = "false"
                    )
            }
    )
    public static class NoteInput implements Structure, Mapped<Note>
    {
        public static final String TONE = "tone";
        public static final String OCTAVE = "octave";
        public static final String SHARP = "sharp";
        public static final String FLAT = "flat";

        private final int toneValue;

        private final Note.Tone tone;
        private final int octave;
        private final boolean sharp;
        private final boolean flat;

        public NoteInput(Note.Tone tone, int octave, boolean sharp, boolean flat)
        {
            this.toneValue = -1;
            this.tone = tone;
            this.octave = octave;
            if (sharp && flat)
                this.sharp = this.flat = false;
            else
            {
                this.sharp = sharp;
                this.flat = flat;
            }
        }

        public NoteInput(int toneValue)
        {
            if (toneValue < 0 || toneValue > 24)
                throw new IllegalActionInputException(BlockPlayNoteAction.IN_NOTE, "Tone value has to be between 0 and 24");
            this.toneValue = toneValue;
            this.tone = null;
            this.octave = 0;
            this.sharp = false;
            this.flat = false;
        }

        public static NoteInput fromNote(Note note)
        {
            return new NoteInput(note.getTone(), note.getOctave(), note.isSharped(), false);
        }

        public static NoteInput deserialize(StructureSerializer ser, StructuredYamlNode node) throws YamlParsingException
        {
            Note.Tone tone = node.get(TONE).getAs(StructureMappers.enumName(Note.Tone.class));
            int octave = node.get(OCTAVE).asInteger(0);
            boolean sharp = node.get(SHARP).asBoolean(false);
            boolean flat = node.get(FLAT).asBoolean(false);

            return new NoteInput(tone, octave, sharp, flat);
        }

        public Note getNote()
        {
            if (this.toneValue != -1)
                return new Note(this.toneValue);
            assert this.tone != null;

            if (this.octave < 0 || this.octave > 2 || (this.octave == 2 && !(this.tone == Note.Tone.F && this.sharp)))
                throw new IllegalActionInputException(BlockPlayNoteAction.IN_NOTE, "Tone and octave have to be between F#0 and F#2");

            if (this.sharp)
                return Note.sharp(this.octave, this.tone);
            else if (this.flat)
                return Note.flat(this.octave, this.tone);
            else
                return Note.natural(this.octave, this.tone);
        }

        @Override
        public void applyTo(@NotNull Note object)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        @SuppressWarnings("deprecation")
        public boolean isAdequate(@Nullable Note object, boolean isStrict)
        {
            if (object == null)
                return false;
            byte thisNote = this.getNote().getId();
            byte thatNote = object.getId();

            return thisNote == thatNote;
        }

        @Override
        public boolean canApplyTo(@Nullable Object target)
        {
            return target instanceof Note;
        }

        public Map<String, Object> serialize()
        {
            Map<String, Object> map = new HashMap<>();
            Note note = this.getNote();
            map.put(TONE, note.getTone());
            map.put(OCTAVE, note.getOctave());
            map.put(SHARP, note.isSharped());
            map.put(FLAT, this.flat);
            return map;
        }
    }
}
