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

(
fork{
	SynthDef.new(\filtNoise, {
		arg freq = 440;
		var sig, frArr = [1, 2.5, 3.5, 4.5, 6.5, 7.5], bpArr, peak;
		sig = In.ar(2);//WhiteNoise.ar();
		peak = Amplitude.ar(sig,0.025,0.025);
		peak = peak * EnvGen.ar(Env.adsr(0.01,0,1,0.01), peak.madd(1,-0.005).max(0));
		peak = peak.madd(10);
		//peak = Limiter.ar(peak);
		10.do({peak = LPF.ar(peak,40);});
		//peak = EnvGen.kr(Env.adsr(0.025,0,1,0.025), peak.madd(1,-0.01).max(0));
		//peak = EnvGen.kr(Env.adsr(1,0,1,1), peak);

		//sig = CombC.ar(sig,0.2,1/freq.lag(0.05),3);
		1.do({
			bpArr = Array.fill(frArr.size, {arg i;
				var unfilt = sig;

				50.do({
					unfilt = BPF.ar(unfilt, frArr[i] * freq.lag(0.05),0.01);
				});
				//unfilt = Compander.ar(unfilt,unfilt,0.1,1,0.1);
				unfilt = Normalizer.ar(unfilt).madd(1/6.0);

				unfilt;
			});
			sig = Mix.ar(bpArr);
		});
		//sig = BPF.ar(sig, freq.lag(0.05));
		sig = sig.madd(\amp.kr(1).lag(0.05) * peak);
		Out.ar(0,sig.dup);
	}).add;



	s.sync;

	a = Synth.new(\filtNoise,[\freq, 800, \amp, 1]);
}
)
a.set(\freq, 1000)
a.set(\freq, 1000* 8 / 7)

