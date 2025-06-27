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
		arg buff = 0, origMid = 60, mid = 60, amp = 0.125, gate = 1.0;
	var rate, sig, env, envGen;
	rate = BufRateScale.kr(buff) * mid.midicps / origMid.midicps;
	sig = PlayBuf.ar(2,buff, rate, doneAction: Done.freeSelf);
	sig = sig.madd(amp); //amp
	env = Env.adsr(0,0,1,0.25);//really just using this for release phase
	envGen = EnvGen.ar(env,gate,doneAction:Done.freeSelf);
	sig = sig.madd(envGen);
	Out.ar(0, sig);
}).writeDefFile("/Users/maestro/Documents/Coding/Eternal_Present/sc_data/");

SynthDef.new(\delay, {
	arg inBus = 0, outBus = 0, delayTime = 0.5, decayTime = 3;
	var sig = In.ar(inBus,2);
	sig = CombC.ar(sig, delayTime,delayTime,decayTime);
	sig = Mix.ar(sig);
	Out.ar(outBus,Pan2.ar(sig,SinOsc.ar(1/(3.0.rand + 2.0))));
}).writeDefFile("/Users/maestro/Documents/Coding/Eternal_Present/sc_data/");

60.2.midicps

SynthDef.new(\pno, {
		arg buff = 0, origMid = 60, mid = 60, vol, key;
	Out.ar(0,PlayBuf.ar(2,buff, BufRateScale.kr(buff) * mid.midicps / origMid.midicps, doneAction: Done.freeSelf));
}).add;

Synth.new("pno",["buff",3,"origMid",59,"mid",59],0,0);

s.sendMsg("/s_new", "pno", 1003, 0, 0, "buff", "2","origMid",59,"mid",59);

s.sendMsg("/s_new", "pno",100,0,0);

SynthDescLib.global.synthDescs["pno"].controls.do(_.postln);

s.dumpOSC(1);

s.sendMsg("/n_set", 0, "freq", 500);

