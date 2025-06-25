~b0 = Buffer.read(s, "/Users/maestro/Documents/Coding/Eternal_Present/piano_samples/samples/dyn3/3_17.wav");
~b0.play;

(
fork{
	~compiledSynth = SynthDef.new(\pno, {
		arg freq, vol, key;
		Out.ar(0,PlayBuf.ar(1,0,doneAction: Done.freeSelf));
	}).asBytes;


	~file = File("/Users/maestro/Documents/Coding/Eternal Present/sc_data/compiledSynth.txt","w");
	~compiledSynth.do({
		arg byte;
		byte.postln;
		~file.write(byte.asString + "\n");
	});
	s.sync;
	~file.close;
}
)

SynthDef.new(\pno, {
		arg freq, vol, key;
		Out.ar(0,PlayBuf.ar(2,0,doneAction: Done.freeSelf));
	}).add;

Synth.new(\pno);


SynthDef.new(\pno, {
		arg buff = 0, origMid = 60, mid = 60, vol, key;
	Out.ar(0,PlayBuf.ar(2,buff, BufRateScale.kr(buff) * mid.midicps / origMid.midicps, doneAction: Done.freeSelf));
}).writeDefFile("/Users/maestro/Documents/Coding/Eternal_Present/sc_data/");


SynthDef.new(\pno, {
		arg buff = 0, origMid = 60, mid = 60, vol, key;
	Out.ar(0,PlayBuf.ar(2,buff, BufRateScale.kr(buff) * mid.midicps / origMid.midicps, doneAction: Done.freeSelf));
}).add;

Synth.new("pno",["buff",3,"origMid",59,"mid",59],0,0);

s.sendMsg("/s_new", "pno", 1000, 0, 0, "buff", 0,"origMid",59,"mid",59);

s.sendMsg("/s_new", "pno",100,0,0);