SynthDef.new(\pno, {
		arg freq, vol, key;
		Out.ar(0,PlayBuf.ar(2,0,doneAction: Done.freeSelf));
	}).writeDefFile("/Users/maestro/Documents/Coding/Eternal_Present/sc_data/synthDefs1.scsyndef");